package com.newkeshe.service.impl;

import com.newkeshe.dao.*;
import com.newkeshe.entity.*;
import com.newkeshe.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class AdminServiceImpl implements AdminService {

    @Autowired
    UserDao userDao;
    @Autowired
    IvgDao ivgDao;
    @Autowired
    UserIvgDao userIvgDao;
    @Autowired
    TaskDao taskDao;
    @Autowired
    UserTaskDao userTaskDao;

    PasswordEncoder p = new BCryptPasswordEncoder();

    //查看所有用户
    @Override
    public List<Map<String,Object>> findAllUser() {
        List<User> list = userDao.findAll();
        List<Map<String,Object>> result = new ArrayList<>();
        for (User u : list){
            Map map = new BeanMap(u);
            map.put("count", userIvgDao.findCountUserByUserId(u.getId()));
            result.add(map);
        }
        return result;
    }

    //添加用户
    @Override
    public User addUser(User user) {
        if (userDao.findByPhone(user.getPhone()) == null) {
            user.setPassword(p.encode(user.getPassword()));
            if(user.getRole()==1||user.getRole()==2){
                user.setAid(1);
            }
            userDao.save(user);
            user.setPassword("");
            return user;
        } else {
            throw new RuntimeException("电话号已存在!");
        }
    }

    //删除用户
    @Override
    public Boolean rmUser(Integer uId) {
        Optional.ofNullable(userDao.findById(uId))
                .orElseThrow(() -> new RuntimeException("用户不存在,请检查你您的操作."));
        userDao.deleteById(uId);
        return true;
    }

    //修改用户信息
    @Override
    public User modiUserInfo(User user) {
        String phone = userDao.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "发生错误"))
                .getPhone();
        if (user.getPhone().equals(phone) || userDao.findByPhone(user.getPhone()) == null) {
            user.setPassword(p.encode(user.getPassword()));
            if(user.getRole()==1||user.getRole()==2){
                user.setAid(1);
            }
            if(user.getRole()==0){
                user.setAid(0);
            }
            userDao.save(user);
            user.setPassword("");
            return user;
        } else
            throw new RuntimeException("电话号已存在!");
    }

    //添加考试信息
    @Override
    public Ivg addIvg(Ivg ivg) {
        ivgDao.save(ivg);
        return ivg;
    }

    //删除考试信息
    @Override
    public Boolean rmIvg(Integer ivgId) {
        ivgDao.findById(ivgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到"));
        ivgDao.deleteById(ivgId);
        return true;
    }

    //修改考试信息
    @Override
    public Ivg modiIvgInfo(Ivg ivg) {
        ivgDao.save(ivg);
        return ivg;
    }

    //设置监考人员
    @Override
    public User_Ivg setUserIvg(Integer uId, Integer ivgId) {
        if (!userDao.findById(uId).isPresent() || !ivgDao.findById(ivgId).isPresent())
            throw new RuntimeException("参数错误");
        User_Ivg user_ivg = new User_Ivg();
        user_ivg.setUser(new User(uId));
        user_ivg.setIvg(new Ivg(ivgId));
        ivgDao.findById(ivgId).ifPresent(ivg -> {
            if (userIvgDao.findCountIvgByIvgId(ivgId) >= ivg.getNumbersOfTeacher()) {
                throw new RuntimeException("分配人数超过限制");
            }
        });
        userIvgDao.findByUser(new User(uId)).forEach(ui -> {
            //用户已分配的考试的开始时间和结束时间
            LocalDateTime isSetBegin = ui.getIvg().getBeginTime();
            /*LocalDateTime isSetEnd = isSetBegin.plusHours(ui.getIvg().getDuration().getHour())
                    .plusMinutes(ui.getIvg().getDuration().getMinute());*/
            LocalDateTime isSetEnd = ui.getIvg().getEndTime();
            //准备分配的考试的开始和结束时间
            Ivg ivg = ivgDao.findById(ivgId).orElseThrow(() -> new RuntimeException("发生错误"));
            LocalDateTime begin = ivg.getBeginTime();
            /*LocalDateTime end = begin.plusHours(ivg.getDuration().getHour())
                    .plusMinutes(ivg.getDuration().getMinute());*/
            LocalDateTime end = ivg.getEndTime();
            if ((end.isBefore(isSetEnd) && end.isAfter(isSetBegin))
                    || (begin.isBefore(isSetEnd) && begin.isAfter(isSetBegin))
                    || (begin.isBefore(isSetBegin) && end.isAfter(isSetEnd))) {
                userIvgDao.save(user_ivg);
                throw new RuntimeException("信息已保存,但是与时间为" + ui.getIvg().getBeginTime() +
                        "的" + ui.getIvg().getName() + "考试冲突");
            }
        });
        return userIvgDao.save(user_ivg);
    }

    //已分配监考人员数量
    @Override
    public Integer countIsSetIvg(Integer ivgId) {
        return userIvgDao.findCountIvgByIvgId(ivgId);
    }

    //删除监考人员
    @Override
    public Boolean rmUserIvg(Integer id) {
        userIvgDao.deleteById(id);
        return true;
    }

    //添加任务
    @Override
    public Task addTask(Task task) {
        taskDao.save(task);
        return task;
    }

    //删除任务
    @Override
    public Boolean rmTask(Integer tId) {
        taskDao.findById(tId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到任务信息"));
        taskDao.deleteById(tId);
        return true;
    }

    //修改任务
    @Override
    public Task modiTaskInfo(Task task) {
        taskDao.save(task);
        return task;
    }

    //关闭任务
    @Override
    public Task closeTask(Integer tId) {
        taskDao.closeTask(tId);
        return taskDao.findById(tId).orElse(null);
    }

    //设置任务人员
    @Override
    public User_Task setUserTask(Integer uid, Integer tid){
        User_Task user_task = new User_Task();
        user_task.setUser(new User(uid));
        user_task.setTask(new Task(tid));
        return userTaskDao.save(user_task);
    }

    //删除任务信息
    @Override
    public boolean rmUserTask(Integer id) {
        userTaskDao.deleteById(id);
        return true;
    }
}

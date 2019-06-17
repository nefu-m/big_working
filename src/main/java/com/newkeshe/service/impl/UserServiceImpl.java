package com.newkeshe.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.newkeshe.dao.*;
import com.newkeshe.entity.*;
import com.newkeshe.service.UserService;
import com.newkeshe.util.TokenService;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;
    @Autowired
    UserIvgDao userIvgDao;
    @Autowired
    UserTaskDao userTaskDao;
    @Autowired
    TaskDao taskDao;
    @Autowired
    IvgDao ivgDao;

    @Autowired
    TokenService tokenService;

    PasswordEncoder p = new BCryptPasswordEncoder();

    //登录验证
    public Object login(String uPhone, String uPwd) {
        User user = Optional.ofNullable(userDao.findByPhone(uPhone))
                .orElseThrow(() -> new RuntimeException("用户不存在!"));
        if (p.matches(uPwd, user.getPassword())) {
            JSONObject jsonObject = new JSONObject();
            user.setPassword("");
            jsonObject.put("uInfo", user);
            Map<String, String> map = new HashMap<>();
            map.put("uId", user.getId().toString());
            map.put("uAid", user.getAid().toString());
            jsonObject.put("token", tokenService.encrypt(map));
            return jsonObject;
        } else {
            throw new RuntimeException("密码错误!");
        }
    }

    //查找用户
    @Override
    public User findSelf(Integer id) {
        return userDao.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到用户信息"));
    }

    //修改个人信息
    public User ModiPersInfo(User user) {
        String phone = userDao.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到用户信息"))
                .getPhone();
        if (userDao.findByPhone(user.getPhone()) == null || user.getPhone().equals(phone)) {
            user.setPassword(p.encode(user.getPassword()));
            userDao.save(user);
            user.setPassword("");
            return user;
        } else
            throw new RuntimeException("电话号已存在!");
    }

    //查看所有监考
    @Override
    public Object listAllIvg() {
        List<Map> result = new ArrayList<>();
        List<Ivg> list = ivgDao.findAll();
        for (Ivg ivg : list) {
            Map map = new BeanMap(ivg);
            map.put("count", userIvgDao.findCountIvgByIvgId(ivg.getId()));
            result.add(map);
        }
        return result;
    }

    //查看监考人员
    @Override
    public Object viewIvgsUser(Integer ivgId) {
        return Optional.ofNullable(userIvgDao.findByIvg(new Ivg(ivgId))).orElse(new ArrayList<>());
    }

    //查看我的监考
    @Override
    public List<User_Ivg> viewUsersIvg(Integer uId) {
        return Optional.ofNullable(userIvgDao.findByUser(new User(uId))).orElse(new ArrayList<>());
    }

    //设置监考人员
    @Override
    public User_Task setUserTask(User_Task user_task) {
        LocalDateTime ddl = taskDao.findById(user_task.getTask().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                .getDdl();
        LocalDateTime now = LocalDateTime.now();
        user_task.setTimeOut(now.isAfter(ddl));
        return userTaskDao.save(user_task);
    }

    //查看任务信息
    @Override
    public List<User_Task> findSomeoneTaskInfo(Integer uId, Integer tId) {
        return userTaskDao.findByUserAndTask(new User(uId), new Task(tId));
    }

    //回复任务
    @Override
    public User_Task reply(User_Task user_task){
        if(user_task.getTask().getDdl().isBefore(LocalDateTime.now())){
            user_task.setTimeOut(false);
        }else{
            user_task.setTimeOut(true);
        }
        user_task.setUpdateTime(LocalDateTime.now());
        return userTaskDao.save(user_task);
    }

    //查看所有任务
    @Override
    public List<Task> listAllTask() {
        return taskDao.findAll();
    }

    //查看用户任务
    @Override
    public List<User_Task> getUserTask(Integer uId) {
        return userTaskDao.findByUser(new User(uId));
    }

    //查看任务人员
    @Override
    public List<User_Task> getTaskUser(Integer tId) {
        return userTaskDao.findByTask(new Task(tId));
    }
}

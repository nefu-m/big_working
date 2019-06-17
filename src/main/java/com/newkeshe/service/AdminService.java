package com.newkeshe.service;

import com.newkeshe.entity.*;

public interface AdminService {
//    用户信息管理
    Object findAllUser();
    User addUser(User user);
    Boolean rmUser(Integer uId);
    User modiUserInfo(User user);
//    监考信息管理以及监考人员管理
    Ivg addIvg(Ivg ivg);
    Boolean rmIvg(Integer ivgId);
    Ivg modiIvgInfo(Ivg ivg);
    User_Ivg setUserIvg(Integer uId,Integer ivgId);
    Integer countIsSetIvg(Integer ivgId);
    Boolean rmUserIvg(Integer id);
//    任务信息管理以及任务完成情况管理
    Task addTask(Task task);
    Boolean rmTask(Integer tId);
    Task modiTaskInfo(Task task);
    User_Task setUserTask(Integer uid, Integer tid);

    Task closeTask(Integer tId);
//    删除用户完成信息情况
    boolean rmUserTask(Integer id);
}

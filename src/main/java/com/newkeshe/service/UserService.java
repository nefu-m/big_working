package com.newkeshe.service;

import com.newkeshe.entity.*;

import java.util.List;

public interface UserService {
    Object login(String uPhone, String uPwd);
    User findSelf(Integer id);
    User ModiPersInfo(User user);
    Object listAllIvg();
    Object viewIvgsUser(Integer ivgId);
    List<User_Ivg> viewUsersIvg(Integer uId);
    User_Task setUserTask(User_Task user_task);
    List<User_Task> findSomeoneTaskInfo(Integer uId, Integer tId);
    List<Task> listAllTask();
    List<User_Task> getUserTask(Integer uId);
    List<User_Task> getTaskUser(Integer tId);
    User_Task reply(User_Task user_task);
}

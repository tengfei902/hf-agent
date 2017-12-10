package hf.agent.api;

import hf.base.biz.CacheService;
import hf.base.client.DefaultClient;
import hf.base.contants.Constants;
import hf.base.model.UserInfo;
import hf.base.utils.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static hf.base.contants.UserConstants.*;
import static hf.base.contants.UserConstants.GROUP_ID;
import static hf.base.contants.UserConstants.USER_STATUS;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private CacheService cacheService;
    @Autowired
    private DefaultClient client;

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ModelAndView login(HttpServletRequest request, String loginId, String password) {
        UserInfo userInfo = client.getUserInfo(loginId,password, Constants.GROUP_TYPE_AGENT);
        ModelAndView modelAndView = new ModelAndView();

        if(Objects.isNull(userInfo) || Objects.isNull(userInfo.getId())) {
            modelAndView.setViewName("redirect:/login.jsp");
            return modelAndView;
        }

        doLogin(request,userInfo);

        modelAndView.setViewName("redirect:/common/index");
        modelAndView.addObject("userInfo",userInfo);
        modelAndView.addObject("userId",userInfo.getId());
        return modelAndView;
    }

    private void doLogin(HttpServletRequest request,UserInfo userInfo) {
        request.getSession().setAttribute(Constants.USER_LOGIN_INFO,
                MapUtils.buildMap(ID,userInfo.getId(),
                        NAME,userInfo.getName(),
                        LOGIN_ID,userInfo.getLoginId(),
                        USER_TYPE, hf.base.enums.UserType.parse(userInfo.getType()).getDesc(),
                        GROUP_ID,userInfo.getGroupId(),
                        USER_STATUS,userInfo.getStatus()));
        request.getSession().setAttribute("userId",userInfo.getId());
        request.getSession().setAttribute("groupId",userInfo.getGroupId());
        cacheService.login(userInfo.getId().toString(),request.getSession().getId());
    }

    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    public ModelAndView logout(HttpServletRequest request) {
        request.getSession().removeAttribute(Constants.USER_LOGIN_INFO);
        ModelAndView modelAndView = new ModelAndView("redirect:/login.jsp");
        return modelAndView;
    }
}

package hf.agent.api;

import hf.agent.biz.HfClient;
import hf.agent.utils.enums.UserType;
import hf.base.biz.CacheService;
import hf.base.model.UserInfo;
import hf.base.utils.Constants;
import hf.base.utils.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private HfClient hfClient;
    @Autowired
    private CacheService cacheService;

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ModelAndView login(HttpServletRequest request, String loginId, String password) {
        UserInfo userInfo = hfClient.getUserInfo(loginId,password, Constants.GROUP_TYPE_AGENT);
        ModelAndView modelAndView = new ModelAndView();

        if(Objects.isNull(userInfo) || Objects.isNull(userInfo.getId())) {
            modelAndView.setViewName("redirect:/login.jsp");
            return modelAndView;
        }

        request.getSession().setAttribute(hf.base.utils.Constants.USER_LOGIN_INFO, MapUtils.buildMap("id",userInfo.getId(),"name",userInfo.getName(),"loginId",userInfo.getLoginId(),"userType",UserType.parse(userInfo.getType()).getDesc(),"groupId",userInfo.getGroupId()));
        request.getSession().setAttribute("userId",userInfo.getId());
        request.getSession().setAttribute("groupId",userInfo.getGroupId());
        cacheService.login(userInfo.getId().toString(),request.getSession().getId());

        modelAndView.setViewName("redirect:/common/index");
        modelAndView.addObject("userInfo",userInfo);
        modelAndView.addObject("userId",userInfo.getId());
        return modelAndView;
    }

    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    public ModelAndView logout(HttpServletRequest request) {
        request.getSession().removeAttribute(Constants.USER_LOGIN_INFO);
        ModelAndView modelAndView = new ModelAndView("redirect:/login.jsp");
        return modelAndView;
    }
}

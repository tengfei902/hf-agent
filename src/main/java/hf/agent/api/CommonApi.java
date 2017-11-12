package hf.agent.api;

import hf.base.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/common")
public class CommonApi {

    @RequestMapping(value="/{page}")
    public ModelAndView dispatch(HttpServletRequest request, @PathVariable("page")String page) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(String.format("/%s",page));

        if(StringUtils.equals(page,"index")) {
            Map<String,Object> map = (Map<String,Object>)request.getSession().getAttribute(Constants.USER_LOGIN_INFO);
            modelAndView.addObject("name",map.get("name"));
        }

        return modelAndView;
    }
}

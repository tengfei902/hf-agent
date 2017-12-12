package hf.agent.api;

import hf.base.biz.CacheService;
import hf.base.client.DefaultClient;
import hf.base.contants.Constants;
import hf.base.exceptions.BizFailException;
import hf.base.model.*;
import hf.base.utils.MapUtils;
import hf.base.utils.Pagenation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @RequestMapping(value = "/register",method = RequestMethod.POST)
    public ModelAndView register(HttpServletRequest request) {
        Long groupId = Long.parseLong(request.getSession().getAttribute("groupId").toString());
        String loginId = request.getParameter("loginId");
        String password = request.getParameter("password");
        String confirmpassword = request.getParameter("confirmpassword");
        String email = request.getParameter("email");
        String tel = request.getParameter("tel");

        ModelAndView modelAndView = new ModelAndView();

        if(StringUtils.isEmpty(loginId) || StringUtils.isEmpty(password) || StringUtils.isEmpty(confirmpassword)
                || StringUtils.isEmpty(email) || StringUtils.isEmpty(tel)) {
            modelAndView.setViewName("agent_add_user");
            return modelAndView;
        }

        if(!StringUtils.equals(password,confirmpassword)) {
            modelAndView.setViewName("agent_add_user");
            return modelAndView;
        }

        String result = client.register(MapUtils.buildMap("loginId",loginId,
                "password",password,"email",email,"tel",tel,"subGroupId",groupId));

        if(StringUtils.equals(result,"0000000")) {
            modelAndView.setViewName("agent_add_user");
            return modelAndView;
        } else {
            modelAndView.setViewName("agent_add_user");
            return modelAndView;
        }
    }

    @RequestMapping(value = "/getOrderRecord",method = RequestMethod.POST)
    public ModelAndView getOrderRecord(HttpServletRequest request) {
        Long groupId = Long.parseLong(request.getSession().getAttribute("groupId").toString());

        TradeRequest tradeRequest = new TradeRequest();
        tradeRequest.setGroupId(groupId);
        int currentPage = 1;
        if(StringUtils.isNotEmpty(request.getParameter("currentPage"))) {
            currentPage = Integer.parseInt(request.getParameter("currentPage"));
        }

        tradeRequest.setCurrentPage(currentPage);
        tradeRequest.setPageSize(15);
        if(StringUtils.isNotEmpty(request.getParameter("mchId"))) {
            tradeRequest.setMchId(request.getParameter("mchId"));
        }

        if(StringUtils.isNotEmpty(request.getParameter("channelCode"))) {
            tradeRequest.setChannelCode(request.getParameter("channelCode"));
        }
        if(StringUtils.isNotEmpty(request.getParameter("status"))) {
            tradeRequest.setStatus(Integer.parseInt(request.getParameter("status")));
        }
        if(StringUtils.isNotEmpty(request.getParameter("outTradeNo"))) {
            tradeRequest.setOutTradeNo(request.getParameter("outTradeNo"));
        }
        if(StringUtils.isNotEmpty(request.getParameter("type"))) {
            tradeRequest.setType(Integer.parseInt(request.getParameter("type")));
        }

        Pagenation<TradeRequestDto> pagenation =  client.getTradeList(tradeRequest);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("agent_order_record");
        modelAndView.addObject("pageInfo",pagenation);
        modelAndView.addObject("requestInfo",tradeRequest);

        List<UserChannel> channels = client.getUserChannelList(groupId);
        modelAndView.addObject("channels",channels);

        return modelAndView;
    }

    @RequestMapping(value = "/withdraw_caculate",method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
    public @ResponseBody Map<String,Object> withDrawCaculate(HttpServletRequest request) {
        BigDecimal settleAmount = new BigDecimal(request.getParameter("settleAmount"));
        BigDecimal withDrawRate = client.getWithDrawRate();
        BigDecimal fee = settleAmount.multiply(withDrawRate).divide(new BigDecimal("100"),2,BigDecimal.ROUND_HALF_UP);
        BigDecimal amount = settleAmount.subtract(fee);

        return MapUtils.buildMap("status",true,"brokerage",fee,"amount",amount);
    }

    @RequestMapping(value = "/submit_withdraw",method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
    public @ResponseBody Map<String,Object> submitWithDraw(HttpServletRequest request) {
        Long groupId = Long.parseLong(request.getSession().getAttribute("groupId").toString());
        BigDecimal settleAmount = new BigDecimal(request.getParameter("settleAmount")).multiply(new BigDecimal("100"));
        Long cardId = Long.parseLong(request.getParameter("cardId"));

        List<UserBankCard> cardList = client.getUserBankCard(groupId);
        List<Long> cardIds = cardList.parallelStream().map(UserBankCard::getId).collect(Collectors.toList());
        if(!cardIds.contains(cardId)) {
            return MapUtils.buildMap("status",false,"msg","结算卡错误");
        }

        Account account = client.getAccountByGroupId(groupId);
        BigDecimal availableAmount = account.getAmount().subtract(account.getLockAmount());
        if(availableAmount.compareTo(settleAmount)<0) {
            return MapUtils.buildMap("status",false,"msg","最大结算结算金额:"+availableAmount.divide(new BigDecimal("100"),2,BigDecimal.ROUND_HALF_UP));
        }

        try {
            Boolean result = client.newSettleRequest(groupId,cardId,settleAmount);
            return MapUtils.buildMap("status",result);
        } catch (BizFailException e) {
            return MapUtils.buildMap("status",false,"msg",e.getMessage());
        }
    }

    @RequestMapping(value = "/getWithDrawList",method = RequestMethod.POST ,produces = "application/json;charset=UTF-8")
    public ModelAndView getWithDrawList(HttpServletRequest request) {
        String groupId = request.getSession().getAttribute("groupId").toString();
        String status = request.getParameter("status");
        Integer pageSize = 15;
        Integer currentPage = 1;
        WithDrawRequest withDrawRequest = new WithDrawRequest();
        withDrawRequest.setPageSize(pageSize);
        withDrawRequest.setCurrentPage(currentPage);
        withDrawRequest.setGroupId(Long.parseLong(groupId));
        if(StringUtils.isNotEmpty(status)) {
            withDrawRequest.setStatus(Integer.parseInt(status));
        }
        if(StringUtils.isNotEmpty(request.getParameter("mchId"))) {
            withDrawRequest.setMchId(request.getParameter("mchId"));
        }

        Pagenation<WithDrawInfo> pagenation = client.getWithDrawPage(withDrawRequest);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("agent_withdraw_record");
        modelAndView.addObject("pageInfo",pagenation);
        modelAndView.addObject("requestInfo",withDrawRequest);
        return modelAndView;
    }
}

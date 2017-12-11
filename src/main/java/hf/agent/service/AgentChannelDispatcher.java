package hf.agent.service;

import hf.base.client.DefaultClient;
import hf.base.dispatcher.DispatchResult;
import hf.base.dispatcher.Dispatcher;
import hf.base.model.UserChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class AgentChannelDispatcher implements Dispatcher {
    @Autowired
    private DefaultClient client;

    @Override
    public DispatchResult dispatch(HttpServletRequest request, String page) {
        Long groupId = Long.parseLong(request.getSession().getAttribute("groupId").toString());
        List<UserChannel> channels = client.getUserChannelList(groupId);

        DispatchResult dispatchResult = new DispatchResult();
        dispatchResult.setPage(page);
        dispatchResult.addObject("channels",channels);
        return dispatchResult;
    }
}

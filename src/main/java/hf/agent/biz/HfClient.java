package hf.agent.biz;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hf.base.client.BaseClient;
import hf.base.model.RemoteParams;
import hf.base.model.UserInfo;
import hf.base.utils.ResponseResult;
import org.springframework.stereotype.Component;

@Component
public class HfClient extends BaseClient {

    private static final String GET_USER_INFO = "http://127.0.0.1:8080/jh/user/get_user_info";

    public UserInfo getUserInfo(String loginId, String password, int userType) {
        RemoteParams params = new RemoteParams().withPath(GET_USER_INFO).withParam("loginId",loginId).withParam("password",password).withParam("userType",userType);

        String result = super.post(params);
        ResponseResult<UserInfo> response = new Gson().fromJson(result,new TypeToken<ResponseResult<UserInfo>>(){}.getType());
        UserInfo userInfo = response.getData();
        if(userInfo == null || userInfo.getId() == null) {
            return null;
        }

        return userInfo;
    }
}

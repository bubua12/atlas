package com.bubua12.atlas.auth.service;

import com.bubua12.atlas.auth.form.LoginBody;
import com.bubua12.atlas.auth.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginBody loginBody);

    void logout(String token);

    LoginVO refreshToken(String token);
}

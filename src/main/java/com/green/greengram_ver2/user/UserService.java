package com.green.greengram_ver2.user;

import com.green.greengram_ver2.common.MyFileUtils;
import com.green.greengram_ver2.user.model.UserSignInReq;
import com.green.greengram_ver2.user.model.UserSignInRes;
import com.green.greengram_ver2.user.model.UserSignUpReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper mapper;
    private final MyFileUtils myFileUtils;

    public int postSignUp (MultipartFile pic , UserSignUpReq p){
        String savedPicName = (pic != null ? myFileUtils.makeRandomFileName(pic) : null);
        String hashedPassword = BCrypt.hashpw(p.getUpw(),BCrypt.gensalt());
        p.setPic(savedPicName);
        p.setUpw(hashedPassword);

        int result = mapper.insUser(p);
        if (pic == null){
            return result;
        }
        long userId = p.getUserId();
        String middlePath = String.format("user/%d",userId);
        myFileUtils.makeFolders(middlePath);
        String filePath = String.format("%s/%s",middlePath,savedPicName);
        try {
            myFileUtils.transferTo(pic , filePath);
            // 임시파일에 있는 pic 을 filepath (저장하고 싶은 경로) 위치로
        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }

    public UserSignInRes selUserForSignIn (UserSignInReq p){
        UserSignInRes res = mapper.selUserByUid(p.getUid());

        if (res == null){
            res = new UserSignInRes();
            res.setMessage("아이디를 확인해 주세요");
            return res;
        } else if (!BCrypt.checkpw(p.getUpw() , res.getUpw())){
            res = new UserSignInRes();
            res.setMessage("비밀번호를 확인해 주세요");
            return res;
        }
        res.setMessage("로그인 성공~");
        return res;
    }
}

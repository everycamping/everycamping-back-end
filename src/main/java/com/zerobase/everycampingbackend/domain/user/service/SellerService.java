package com.zerobase.everycampingbackend.domain.user.service;

import static com.zerobase.everycampingbackend.domain.auth.issuer.JwtIssuer.REFRESH_EXPIRE_TIME;

import com.zerobase.everycampingbackend.domain.admin.service.SellerRequestService;
import com.zerobase.everycampingbackend.domain.auth.dto.JwtDto;
import com.zerobase.everycampingbackend.domain.auth.issuer.JwtIssuer;
import com.zerobase.everycampingbackend.domain.auth.service.CustomUserDetailsService;
import com.zerobase.everycampingbackend.domain.auth.type.UserType;
import com.zerobase.everycampingbackend.domain.redis.RedisClient;
import com.zerobase.everycampingbackend.domain.user.dto.SellerDto;
import com.zerobase.everycampingbackend.domain.user.entity.Seller;
import com.zerobase.everycampingbackend.domain.user.form.PasswordForm;
import com.zerobase.everycampingbackend.domain.user.form.SignInForm;
import com.zerobase.everycampingbackend.domain.user.form.SignUpForm;
import com.zerobase.everycampingbackend.domain.user.form.UserInfoForm;
import com.zerobase.everycampingbackend.domain.user.repository.SellerRepository;
import com.zerobase.everycampingbackend.exception.CustomException;
import com.zerobase.everycampingbackend.exception.ErrorCode;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerService implements CustomUserDetailsService {

    public static final String RT_REDIS_INDEX = "RT-SELLER";
    private final SellerRepository sellerRepository;
    private final JwtIssuer jwtIssuer;
    private final PasswordEncoder passwordEncoder;
    private final RedisClient redisClient;
    private final SellerRequestService sellerRequestService;

    public void signUp(SignUpForm form) {
        if (sellerRepository.existsByEmail(form.getEmail().toLowerCase(Locale.ROOT))) {
            throw new CustomException(ErrorCode.EMAIL_BEING_USED);
        }

        Seller seller = Seller.from(form, passwordEncoder);
        sellerRepository.save(seller);
        sellerRequestService.applySellerRequest(seller);
    }

    public JwtDto signIn(SignInForm form) {
        Seller seller = getSellerByEmail(form.getEmail().toLowerCase(Locale.ROOT));

        if (!passwordEncoder.matches(form.getPassword(), seller.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_CHECK_FAIL);
        }

        return issueJwt(seller.getEmail(), seller.getId());
    }

    public void signOut(String email) {
        deleteRefreshToken(email);
    }

    public void updateInfo(Seller seller, UserInfoForm form) {
        seller.setNickName(form.getNickName());
        seller.setPhone(form.getPhoneNumber());
        seller.setAddress(form.getAddress());
        seller.setZipcode(form.getZipcode());
        sellerRepository.save(seller);
    }

    public SellerDto getInfo(Seller seller){
        return SellerDto.from(seller);
    }

    public void updatePassword(Seller seller, PasswordForm form) {
        if(!passwordEncoder.matches(form.getOldPassword(), seller.getPassword())){
            throw new CustomException(ErrorCode.USER_NOT_EDITOR);
        }
        seller.setPassword(passwordEncoder.encode(form.getNewPassword()));
        sellerRepository.save(seller);
    }

    @Override
    public JwtDto issueJwt(String email, Long id) {
        JwtDto jwtDto = jwtIssuer.createToken(email, id, UserType.SELLER.name());
        putRefreshToken(email, jwtDto.getRefreshToken());
        return jwtDto;
    }

    public Seller getSellerById(Long id) {
        return sellerRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public Seller getSellerByEmail(String email) {
        return sellerRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return getSellerByEmail(email);
    }

    @Override
    public String getRefreshToken(String email) {
        return redisClient.getValue(RT_REDIS_INDEX, email);
    }

    private void putRefreshToken(String email, String token) {
        redisClient.putValue(RT_REDIS_INDEX, email, token, REFRESH_EXPIRE_TIME);
    }

    private void deleteRefreshToken(String email) {
        redisClient.deleteValue(RT_REDIS_INDEX, email);
    }
}

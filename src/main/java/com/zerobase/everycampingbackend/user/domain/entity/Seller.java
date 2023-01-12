package com.zerobase.everycampingbackend.user.domain.entity;

import com.zerobase.everycampingbackend.common.BaseEntity;
import com.zerobase.everycampingbackend.user.domain.form.SignUpForm;
import java.time.LocalDateTime;
import java.util.Locale;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seller extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String email;    // 이메일
  private String nickName; // 닉네임
  private String password; // 패스워드
  private String address;  // 주소
  private String zipcode;  // 우편번호
  private String phone;    // 연락처

  private LocalDateTime deleteAt;  // 탈퇴일

  public static Seller from(SignUpForm form, PasswordEncoder passwordEncoder) {
    return Seller.builder()
        .email(form.getEmail().toLowerCase(Locale.ROOT))
        .password(passwordEncoder.encode(form.getPassword()))
        .nickName(form.getNickName())
        .phone(form.getPhoneNumber())
        .build();
  }

}

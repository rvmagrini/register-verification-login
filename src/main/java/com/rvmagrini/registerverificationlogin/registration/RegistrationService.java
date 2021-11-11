package com.rvmagrini.registerverificationlogin.registration;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rvmagrini.registerverificationlogin.appuser.AppUser;
import com.rvmagrini.registerverificationlogin.appuser.AppUserRole;
import com.rvmagrini.registerverificationlogin.appuser.AppUserService;
import com.rvmagrini.registerverificationlogin.registration.token.ConfirmationToken;
import com.rvmagrini.registerverificationlogin.registration.token.ConfirmationTokenService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RegistrationService {
	
	private final AppUserService appUserService;
	private final EmailValidator emailValidator;
	private final ConfirmationTokenService confirmationTokenService;

	public String register(RegistrationRequest request) {
		boolean isValidEmail = emailValidator.test(request.getEmail());
		
		if (!isValidEmail) {
			throw new IllegalStateException("email not valid");
		}
				
		
		return appUserService.signUpUser(new AppUser(
				request.getFirstName(),
				request.getLastName(),
				request.getEmail(),
				request.getPassword(),
				AppUserRole.USER));
	}
	
	
	@Transactional
	public String confirmToken(String token) {
		ConfirmationToken confirmationToken = confirmationTokenService
				.getToken(token)
				.orElseThrow(() -> new IllegalStateException("token not found"));
		
		if (confirmationToken.getConfirmedAt() != null) {
			throw new IllegalStateException("email already confirmed");
		}
		
		LocalDateTime expiredAt = confirmationToken.getExpiresAt();
		
		if (expiredAt.isBefore(LocalDateTime.now())) {
			throw new IllegalStateException("token expired");
		}
		
		confirmationTokenService.setConfirmedAt(token);
		appUserService.enableAppUser(confirmationToken.getAppUser().getEmail());
		
		return "confirmed";
		
	}
	
	

}

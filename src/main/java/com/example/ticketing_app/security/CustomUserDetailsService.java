package com.example.ticketing_app.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.ticketing_app.entity.User;
import com.example.ticketing_app.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String normalized = username == null ? null : username.trim().toLowerCase();
		User user = userRepository.findByEmail(normalized)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		return UserPrincipal.from(user);
	}

	public UserPrincipal loadUserByUserId(String userId) {
		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		return UserPrincipal.from(user);
	}
}


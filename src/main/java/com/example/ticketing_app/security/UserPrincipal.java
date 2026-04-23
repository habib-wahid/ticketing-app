package com.example.ticketing_app.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.ticketing_app.entity.User;
import com.example.ticketing_app.entity.UserRole;

public class UserPrincipal implements UserDetails {

	private final String userId;
	private final String email;
	private final String passwordHash;
	private final UserRole role;
	private final boolean active;

	public UserPrincipal(String userId, String email, String passwordHash, UserRole role, boolean active) {
		this.userId = userId;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
		this.active = active;
	}

	public static UserPrincipal from(User user) {
		return new UserPrincipal(user.getUserId(), user.getEmail(), user.getPasswordHash(), user.getRole(), user.isActive());
	}

	public String getUserId() {
		return userId;
	}

	public UserRole getRole() {
		return role;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		String roleName = role == UserRole.ADMIN ? "ROLE_ADMIN" : "ROLE_USER";
		return List.of(new SimpleGrantedAuthority(roleName));
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return active;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return active;
	}
}


package net.isger.brick.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuthInfo {

    private List<String> roles;

    private List<String> permissions;

    public AuthInfo() {
        this.roles = new ArrayList<String>();
        this.permissions = new ArrayList<String>();
    }

    public List<String> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public void addRole(String role) {
        this.roles.add(role);
    }

    public void addRoles(String... roles) {
        for (String role : roles) {
            this.roles.add(role);
        }
    }

    public void setRoles(List<String> roles) {
        if (roles != null) {
            this.roles.clear();
            this.roles.addAll(roles);
        }
    }

    public void addPermission(String permission) {
        this.permissions.add(permission);
    }

    public void addPermissions(String... permissions) {
        for (String permission : permissions) {
            this.permissions.add(permission);
        }
    }

    public List<String> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }

    public void setPermissions(List<String> permissions) {
        this.permissions.clear();
        if (permissions != null) {
            this.permissions.addAll(permissions);
        }
    }

}

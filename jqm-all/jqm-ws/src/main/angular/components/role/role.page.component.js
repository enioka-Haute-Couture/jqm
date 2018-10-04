'use strict';

import template from './role.page.template.html';


class RolePageCtrl {
    constructor(µRoleDto) {
        this.roles = [];
        this.selected_role = null;
        this.selected_perm = null;

        this.µRoleDto = µRoleDto;
        this.refresh();
    }

    newitem() {
        var t = new this.µRoleDto({
            description: 'what the role does',
            name: "rolename",
            permissions: [],
        });
        this.roles.push(t);
        this.selected_role = t;
    };

    saveitem() {
        this.selected_role.$save(this.refresh.bind(this));
    };

    refresh() {
        this.selected_role = null;
        this.roles = this.µRoleDto.query(this.refreshdone.bind(this));
    };

    refreshdone() {
        if (this.roles.length > 0) {
            this.selected_role = this.roles[0];
            this.selected_perm = this.selected_role.permissions.length > 0 ? this.selected_role.permissions[0] : null;
        }
        else {
            this.roles = [];
            this.selected_role = null;
        }
    };

    deleteitem() {
        if (!this.selected_role) {
            return;
        }
        if (this.selected_role.id) {
            this.selected_role.$remove({ id: this.selected_role.id });
        }
        this.roles.splice(this.roles.indexOf(this.selected_role), 1);
        this.selected_role = null;
    };

    addperm() {
        this.selected_role.permissions.push(this.noun + ":" + this.verb);
    };

    removeperm() {
        if (!this.selected_role || !this.selected_perm) {
            return;
        }
        this.selected_role.permissions.splice(this.selected_role.permissions.indexOf(this.selected_perm), 1);
        this.selected_perm = this.selected_role.permissions.length > 0 ? this.selected_role.permissions[0] : null;
    };
};
RolePageCtrl.$inject = ["µRoleDto"];


export const rolePageComponent = {
    controller: RolePageCtrl,
    template: template,
    bindings: {}
};

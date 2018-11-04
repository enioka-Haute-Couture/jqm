'use strict';

import template from './user.page.template.html';
import { createGlobalFilter } from '../../helpers/filters';


class UserPageCtrl
{
    constructor(µUserDto, µRoleDto)
    {
        this.users = [];
        this.roles = [];

        this.selected_user = null;

        this.µUserDto = µUserDto;
        this.µRoleDto = µRoleDto;

        this.refresh();
    }

    saveitem()
    {
        this.selected_user.$save(this.refresh.bind(this));
    };

    deleteitem()
    {
        if (this.selected_user == null)
        {
            return;
        }
        if (this.selected_user.id != undefined)
        {
            this.selected_user.$remove({
                id: this.selected_user.id
            });
        }
        this.users.splice(this.users.indexOf(this.selected_user), 1);
        this.selected_user = this.users[0];
    };

    newitem()
    {
        var t = new this.µUserDto({
            login: 'login',
            locked: false,
            internal: false,
            expirationDate: (new Date()).addDays(3650),
            creationDate: new Date(),
            freeText: 'user name or service name',
        });
        this.users.push(t);
        angular.element(document.querySelector("#userSelect")).controller('ngModel').$render();
        this.selected_user = t;
        console.debug(this.users);

    };

    refreshdone()
    {
        if (this.users.length > 0)
        {
            this.selected_user = this.users[0];
        }
        else
        {
            this.selected_user = undefined;
        }
        console.debug(this.users);
    };

    refresh()
    {
        this.users = this.µUserDto.query(this.refreshdone.bind(this));
        this.roles = this.µRoleDto.query();
    };
}
UserPageCtrl.$inject = ["µUserDto", "µRoleDto"];


export const userPageComponent = {
    controller: UserPageCtrl,
    template: template,
    bindings: {}
};

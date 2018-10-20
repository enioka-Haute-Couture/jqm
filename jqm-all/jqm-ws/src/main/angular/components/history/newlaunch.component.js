'use strict';

import template from './newlaunch.template.html';


class NewLaunchController
{
    constructor($http, µUserJdDto, µPermManager)
    {
        this.jds = µUserJdDto.query();
        this.selectedJd = null;
        this.$http = $http;
        this.me = µPermManager;

        this.data = {
            selectedJd: null,
            newKey: null,
            newValue: null
        };

        var $ctrl = this;
        this.request = {
            user: null,
            sessionID: 0,
            parameters: [],
            startState: 'SUBMITTED',
            module: "JQM web UI"
        };
        µPermManager.perms.$promise.then(function () { $ctrl.request.user = µPermManager.perms.enforced ? µPermManager.perms.login : 'webuser'; });
    }

    addPrm()
    {
        var np = {};
        np.key = this.data.newKey;
        np.value = this.data.newValue;
        this.request.parameters.push(np);
        this.data.newKey = null;
        this.data.newValue = null;
    };

    postOk(response)
    {
        this.show = false;
        if (this.onLaunched)
        {
            this.onLaunched(response.data);
        }
    };

    ok()
    {
        this.request.applicationName = this.selectedJd.applicationName;
        this.$http.post("ws/client/ji", this.request).then(this.postOk.bind(this));
    };

    cancel()
    {
        this.show = false;
    };
}
NewLaunchController.$inject = ['$http', 'µUserJdDto', 'µPermManager'];



export const newLaunchComponent = {
    controller: NewLaunchController,
    template: template,
    bindings: {
        'show': '=',
        'onLaunched': '<',
    }
};

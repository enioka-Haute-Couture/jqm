'use strict';

import template from './alert.template.html';


var jqmAlertModule = angular.module('jqmAlertModule', []);

class AlertHolderService
{
    constructor()
    {
        this.alertData = {};
    }

    setAlert(alert)
    {
        this.alertData.data = alert;
    }
}


class AlertConfig
{
    constructor($httpProvider)
    {
        $httpProvider.interceptors.push(['$q', 'jqmAlertHolderService', function ($q, jqmAlertHolderService)
        {
            return {
                'responseError': function (rejection)
                {
                    if (rejection.status === 400 || rejection.status === 500)
                    {
                        jqmAlertHolderService.setAlert(rejection.data);
                    }
                    return $q.reject(rejection);
                },
            };
        }]);
    }
}
AlertConfig.$inject = ['$httpProvider',];


class AlertController
{
    constructor(jqmAlertHolderService)
    {
        this.isCollapsed = true;
        this.alertHolder = jqmAlertHolderService;
    }

    closeAlert()
    {
        this.isCollapsed = true;
        this.alertHolder.alertData.data.userReadableMessage = null;
    };

    toggleDetail()
    {
        this.isCollapsed = !this.isCollapsed;
    };
};
AlertController.$inject = ['jqmAlertHolderService',];


var alertComponent = {
    controller: AlertController,
    template: template,
    bindings: {}
};


jqmAlertModule
    .config(AlertConfig)
    .service('jqmAlertHolderService', AlertHolderService)
    .component('jqmAlert', alertComponent)
    .filter('j2h', function ()
    {
        return function (text)
        {
            if (!text)
            {
                return null;
            }
            return text.replace(/\r\n/g, '<br/>').replace(/\n/g, '<br/>').replace(/\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;');
        };
    });

export default "jqmAlertModule";

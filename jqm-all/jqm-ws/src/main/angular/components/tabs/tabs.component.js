'use strict';

import 'bootstrap/js/dist/tab';
import template from './tabs.template.html';


// µPermManager
// module.controller('TabsCtrl', ['$location', '$http', function TabsCtrl($location, $http)
class TabsCtrl
{
    constructor($location)
    {
        this.tabs = [{
            link: '#!/home',
            icon: 'fas fa-home',
            label: 'Home',
            permission: '',
        }, {
            link: '#!/node',
            icon: 'fas fa-microchip',
            label: 'Nodes',
            permission: 'node:read',
        }, {
            link: '#!/q',
            icon: 'fas fa-list-ol',
            label: 'Queues',
            permission: 'queue:create',
        }, {
            link: '#!/qmapping',
            icon: 'fas fa-exchange-alt',
            label: 'Mappings',
            permission: 'qmapping:read',
        }, {
            link: '#!/jndi',
            icon: 'fas fa-cog',
            label: 'JNDI Resources',
            permission: 'jndi:read',
        }, {
            link: '#!/prm',
            icon: 'fas fa-wrench',
            label: 'Cluster-wide parameters',
            permission: 'prm:read',
        }, {
            link: '#!/jd',
            icon: 'fas fa-book',
            label: 'Job definitions',
            permission: 'jd:read',
        }, {
            link: '#!/user',
            icon: 'fas fa-users',
            label: 'Users',
            permission: 'user:read',
        }, {
            link: '#!/role',
            icon: 'fas fa-lock',
            label: 'Roles',
            permission: 'role:read',
        }, {
            link: '#!/history',
            icon: 'far fa-eye',
            label: 'Runs',
            permission: 'job_instance:read',
        },];

        this.selectedTab = this.tabs[0];
        var i = 0;
        for (; i < this.tabs.length; i++)
        {
            if (this.tabs[i].link === "#!" + $location.path())
            {
                this.selectedTab = this.tabs[i];
                break;
            }
        }
    }

    setSelectedTab(tab)
    {
        this.selectedTab = tab;
    };

    tabClass(tab)
    {
        if (this.selectedTab == tab)
        {
            return "nav-link active";
        }
        else
        {
            return "nav-link";
        }
    };
}
TabsCtrl.$inject = ['$location',];

export const tabsComponent = {
    controller: TabsCtrl,
    template: template,
    bindings: {
        someInput: '<',
        someOutput: '&'
    }
};

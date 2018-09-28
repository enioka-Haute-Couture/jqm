'use strict';

import 'bootstrap/js/dist/tab';
import template from './tabs.template.html';


// µPermManager
// module.controller('TabsCtrl', ['$location', '$http', function TabsCtrl($location, $http)
class TabsCtrl
{
    constructor($location, $http)
    {
        $http.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

        this.tabs = [{
            link: '#!/home',
            label: '<span class="fas fa-home"></span> Home',
            permission: '',
        }, {
            link: '#!/node',
            label: '<span class="fas fa-microchip"></span> Nodes',
            permission: 'node:read',
        }, {
            link: '#!/q',
            label: '<span class="fas fa-list-ol"></span> Queues',
            permission: 'queue:create',
        }, {
            link: '#!/qmapping',
            label: '<span class="fas fa-exchange-alt"></span> Queue Mappings',
            permission: 'qmapping:read',
        }, {
            link: '#!/jndi',
            label: '<span class="fas fa-cog"></span> JNDI Resources',
            permission: 'jndi:read',
        }, {
            link: '#!/prm',
            label: '<span class="fas fa-wrench"></span> Cluster-wide parameters',
            permission: 'prm:read',
        }, {
            link: '#!/jd',
            label: '<span class="fas fa-book"></span> Job definitions',
            permission: 'jd:read',
        }, {
            link: '#!/user',
            label: '<span class="fas fa-users"></span> Users',
            permission: 'user:read',
        }, {
            link: '#!/role',
            label: '<span class="fas fa-lock"></span> Roles',
            permission: 'role:read',
        }, {
            link: '#!/history',
            label: '<span class="far fa-eye"></span> Runs',
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

        // µPermManager.refresh();
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
TabsCtrl.$inject = ['$location', '$http'];

export const tabsComponent = {
    controller: TabsCtrl,
    template: template,
    bindings: {
        someInput: '<',
        someOutput: '&'
    }
};

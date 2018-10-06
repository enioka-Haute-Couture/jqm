import jqmServicesModule from '../service/data';
import { jqmHelperModule } from '../helpers/grid.js';
import { homeComponent } from './home/home.component.js';
import { tabsComponent } from './tabs/tabs.component.js';
import { nodeListComponent } from './node/node.list.component.js';
import { queueListComponent } from './queue/queue.list.component';
import { jndiListComponent } from './jndi/jndi.list.component';
import { helpTagComponent } from './misc/helptag.component';
import { helpBlockComponent } from './misc/helpblock.component';
import { prmListComponent } from './prm/prm.list.component';
import { jdListComponent } from './jd/jd.list.component';
import { jdPrmsDiagComponent } from './jd/jd.prm.component';
import { userEditPanelComponent } from './user/user.editpanel.component';
import { userPageComponent } from './user/user.page.component';
import { rolePageComponent } from './role/role.page.component';
import { historyPageComponent } from './history/history.page.component';
import { historyDetailComponent } from './history/history.detail.component';
import { modalComponent } from './misc/modal.component';
import { newLaunchComponent } from './history/newlaunch.component';
import { select2Component } from './misc/select2.component';

import modal from 'angular-ui-bootstrap';
import uiGrid from 'angular-ui-grid';
import s from 'ui-select/dist/select';

import 'angular-ui-grid/ui-grid.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import '@fortawesome/fontawesome-free/css/all.css';


var module = angular.module('jqmComponents', [jqmServicesModule, jqmHelperModule, modal, uiGrid, 'ui.grid.edit', 'ui.grid.selection', 'ui.grid.cellNav', 'ui.grid.resizeColumns', 'ui.grid.autoResize', 'ui.grid.pagination', 'ngSanitize'])
    .component('home', homeComponent)

    .component('tabs', tabsComponent)
    .component('nodes', nodeListComponent)
    .component('queues', queueListComponent)
    .component('jndi', jndiListComponent)
    .component('prms', prmListComponent)
    .component('jds', jdListComponent)
    .component('jdprmsdialog', jdPrmsDiagComponent)
    .component('useredit', userEditPanelComponent)
    .component('users', userPageComponent)
    .component('roles', rolePageComponent)
    .component('history', historyPageComponent)
    .component('jidetail', historyDetailComponent)
    .component('newji', newLaunchComponent)

    .component('helptag', helpTagComponent)
    .component('helpblock', helpBlockComponent)
    .component('modal', modalComponent)
    .component('select2', select2Component)

    .filter('unsafe', ['$sce', function ($sce) {
        return function (val) {
            return $sce.trustAsHtml(val);
        };
    }])
    .filter('epoch2date', function () {
        return function (epochms) {
            return new Date(epochms);
        };
    });

Date.prototype.addDays = function (days) {
    var date = new Date(this.valueOf());
    date.setDate(date.getDate() + days);
    return date;
}

export default module.name;

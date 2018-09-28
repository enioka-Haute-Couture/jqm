'use strict';

import template from './home.template.html';


class HomeController
{
	// ($scope, µUserPerms, µPermManager)
  /*
	 * $scope.login = function() { µPermManager.logout();
	 * µPermManager.refresh(); };
	 */
};

export const homeComponent = {
		  controller: HomeController,
		  template: template,
		  bindings: {
			  someInput: '<',
			  someOutput: '&'
			}
};

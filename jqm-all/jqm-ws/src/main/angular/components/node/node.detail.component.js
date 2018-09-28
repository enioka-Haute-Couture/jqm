'use strict';

import template from './node.list.template.html';


class NodeDetailCtrl
{
	constructor($routeParams, µNodeDto)
	{
		this.nodeId = $routeParams.nodeId;
		this.error = null;

		this.node = µNodeDto.get({
			id: $routeParams.nodeId
		}, function () { }, this.onError);
	}


	onError(errorResult)
	{
		console.debug(errorResult);
		this.error = errorResult.data;
	};
};
NodeDetailCtrl.$inject = ['$routeParams', 'µNodeDto'];

export const NodeDetailComponent = {
	controller: NodeDetailCtrl,
	template: template,
	bindings: {
		someInput: '<',
		someOutput: '&'
	}
};

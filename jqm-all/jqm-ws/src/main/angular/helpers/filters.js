export function createGlobalFilter($scope, columns)
{
	return function (renderableRows)
	{
		if (!$scope.filterValue)
		{
			return renderableRows;
		}
		var matcher = new RegExp($scope.filterValue.toLowerCase());
		renderableRows.forEach(function (row)
		{
			var match = false;
			columns.forEach(function (field)
			{
				if (row.entity[field] && row.entity[field].toLowerCase().match(matcher))
				{
					match = true;
				}
			});
			if (!match)
			{
				row.visible = false;
			}
		});
		return renderableRows;
	};
}
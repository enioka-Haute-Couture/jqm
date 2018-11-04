import $ from 'jquery';

export const jqmPermissionDirective = ['µPermManager', function (µPermManager)
{
    return {
        restrict: 'A',
        replace: false,
        scope: {
            jqmPermission: "@",
        },
        link: function (scope, element, attrs)
        {
            var perms = µPermManager.perms;

            var shouldDisplay = function ()
            {
                // If the control does not ask for a particular permission,
                // always enable it.
                if (!scope.jqmPermission)
                {
                    return true;
                }

                // If the user has no permission at all, disable the control
                if (!perms.permissions || perms.permissions.length == 0)
                {
                    return false;
                }

                // The permission to look for (requested permission => r_)
                var r_noun = scope.jqmPermission.split(":")[0];
                var r_verb = scope.jqmPermission.split(":")[1];
                var i = 0;
                var found = false;

                for (i = 0; i < perms.permissions.length; i++)
                {
                    var p = perms.permissions[i];
                    var noun = p.split(":")[0];
                    var verb = p.split(":")[1];

                    if ((r_noun == noun || noun == "*") && (r_verb == verb || verb == "*"))
                    {
                        found = true;
                        break;
                    }
                }
                return found;
            };

            var updatePerm = function ()
            {
                if (!shouldDisplay())
                {
                    element.addClass('d-none');
                }
                else
                {
                    element.removeClass('d-none');
                }
            };

            perms.$promise.then(updatePerm);
        }
    };
}];

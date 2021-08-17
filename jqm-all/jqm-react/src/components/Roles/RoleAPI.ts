import { useState, useCallback } from "react";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import { Role } from "./Role";

export const useRoleAPI = () => {
    const [roles, setRoles] = useState<Role[] | null>(null);
    const { displayError, displaySuccess } = useNotificationService();
    const fetchRoles = useCallback(() => {
        APIService.get("/role")
            .then((response) => {
                setRoles(response);
            })
            .catch(displayError);
    }, [displayError]);

    const createRole = useCallback(
        async (newRole: Role) => {
            return APIService.post("/role", newRole)
                .then(() => {
                    fetchRoles();
                    displaySuccess(
                        `Successfully created role: ${newRole.name}`
                    );
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, fetchRoles]
    );

    const deleteRoles = useCallback(
        async (roleIds: number[]) => {
            return await Promise.all(
                roleIds.map((id) => APIService.delete("/role/" + id))
            )
                .then(() => {
                    fetchRoles();
                    displaySuccess(
                        `Successfully deleted role${
                            roleIds.length > 1 ? "s" : ""
                        }`
                    );
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, fetchRoles]
    );

    const updateRole = useCallback(
        async (role: Role) => {
            return APIService.put("/role/" + role.id, role)
                .then(() => {
                    fetchRoles();
                    displaySuccess(`Successfully updated role ${role.name}`);
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, fetchRoles]
    );

    return { roles, fetchRoles, createRole, updateRole, deleteRoles };
};

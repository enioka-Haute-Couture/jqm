import { useCallback, useState } from "react";
import { useTranslation } from "react-i18next";
import { Role } from "./Role";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";

const API_URL = "/admin/role";

export const useRoleAPI = () => {
    const { t } = useTranslation();
    const [roles, setRoles] = useState<Role[] | null>(null);
    const { displayError, displaySuccess } = useNotificationService();
    const fetchRoles = useCallback(() => {
        APIService.get(API_URL)
            .then((response) => {
                setRoles(response);
            })
            .catch(displayError);
    }, [displayError]);

    const createRole = useCallback(
        async (newRole: Role) => {
            return APIService.post(API_URL, newRole)
                .then(() => {
                    fetchRoles();
                    displaySuccess(
                        t("roles.messages.successCreate", {
                            name: newRole.name,
                        })
                    );
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, fetchRoles, t]
    );

    const deleteRoles = useCallback(
        async (roleIds: number[]) => {
            return await Promise.all(
                roleIds.map((id) => APIService.delete(`${API_URL}/${id}`))
            )
                .then(() => {
                    fetchRoles();
                    displaySuccess(
                        t("roles.messages.successDelete", {
                            count: roleIds.length,
                        })
                    );
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, fetchRoles, t]
    );

    const updateRole = useCallback(
        async (role: Role) => {
            return APIService.put(`${API_URL}/${role.id}`, role)
                .then(() => {
                    fetchRoles();
                    displaySuccess(
                        t("roles.messages.successUpdate", { name: role.name })
                    );
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, fetchRoles, t]
    );

    return { roles, fetchRoles, createRole, updateRole, deleteRoles };
};

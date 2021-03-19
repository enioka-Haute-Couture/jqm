import { useState, useCallback } from "react";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import { Role, User } from "./User";

export const useUserAPI = () => {
    const [users, setUsers] = useState<any[] | null>(null);
    const [roles, setRoles] = useState<Role[] | null>(null);
    const { displayError, displaySuccess } = useNotificationService();
    const fetchRoles = useCallback(async () => {
        APIService.get("/role")
            .then((response) => {
                setRoles(response);
            })
            .catch(displayError);
    }, [displayError]);


    const fetchUsers = useCallback(async () => {
        APIService.get("/user")
            .then((response) => {
                setUsers(response);
            })
            .catch(displayError);
    }, [displayError]);

    const createUser = useCallback(
        async (newUser: User) => {
            return APIService.post("/user", newUser)
                .then(() => {
                    fetchUsers();
                    displaySuccess(`Successfully created user: ${newUser.login}`);
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, fetchUsers]
    );

    const deleteUsers = useCallback(
        async (userIds: any[]) => {
            return await Promise.all(
                userIds.map((id) => APIService.delete("/user/" + id))
            )
                .then(() => {
                    fetchUsers();
                    displaySuccess(`Successfully deleted user${userIds.length > 1 ? "s" : ""}`);
                })
                .catch(displayError);
        },
        [displayError, displaySuccess, fetchUsers]
    );

    const updateUser = useCallback(
        async (user: User) => {
            return APIService.put("/user/" + user.id, user)
                .then(() => {
                    fetchUsers();
                    displaySuccess(`Successfully updated user ${user.login}`);
                })
                .catch(displayError)
        },
        [displayError, displaySuccess, fetchUsers]
    );

    const changePassword = useCallback(
        (userId: string) =>
            async (password: string) => {
                return APIService.put("/user/" + userId, { newPassword: password })
                    .then(() => {
                        displaySuccess(`Successfully updated password of user`);
                    })
                    .catch(displayError)
            },
        [displayError, displaySuccess]
    );


    return { users, roles, fetchUsers, fetchRoles, createUser, updateUser, deleteUsers, changePassword };
};

import { useCallback, useState } from "react";
import { ClassLoader } from "./ClassLoader";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import { useTranslation } from "react-i18next";

const API_URL = "/admin/cl";

export const useClassLoaderAPI = () => {
    const { t } = useTranslation();
    const { displayError, displaySuccess } = useNotificationService();

    const [classLoaders, setClassLoaders] = useState<ClassLoader[] | null>();

    const fetchClassLoaders = useCallback(async () => {
        return APIService.get(API_URL)
            .then((classLoaders) => setClassLoaders(classLoaders))
            .catch(displayError);
    }, [displayError]);

    const createClassLoader = useCallback(
        async (newClassLoader: ClassLoader) => {
            return APIService.post(API_URL, newClassLoader)
                .then(() => {
                    fetchClassLoaders();
                    displaySuccess(
                        t("classLoaders.messages.successCreate", {
                            name: newClassLoader.name,
                        })
                    );
                })
                .catch(displayError);
        },
        [fetchClassLoaders, displayError, displaySuccess, t]
    );

    const deleteClassLoaders = useCallback(
        async (classLoaderIds: number[]) => {
            return await Promise.all(
                classLoaderIds.map((id) =>
                    APIService.delete(`${API_URL}/${id}`)
                )
            )
                .then(() => {
                    fetchClassLoaders();
                    displaySuccess(
                        t("classLoaders.messages.successDelete", {
                            count: classLoaderIds.length,
                        })
                    );
                })
                .catch(displayError);
        },
        [fetchClassLoaders, displayError, displaySuccess, t]
    );

    const updateClassLoader = useCallback(
        async (classLoader: ClassLoader) => {
            return APIService.put(`${API_URL}/${classLoader.id}`, classLoader)
                .then(() => {
                    fetchClassLoaders();
                    displaySuccess(t("classLoaders.messages.successSave"));
                })
                .catch(displayError);
        },
        [fetchClassLoaders, displayError, displaySuccess, t]
    );

    return {
        classLoaders,
        fetchClassLoaders,
        createClassLoader,
        updateClassLoader,
        deleteClassLoaders,
    };
};

export default useClassLoaderAPI;

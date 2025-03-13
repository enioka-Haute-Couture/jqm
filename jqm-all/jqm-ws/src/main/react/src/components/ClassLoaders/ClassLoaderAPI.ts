import { useCallback, useState } from "react";
import { ClassLoader } from "./ClassLoader";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";

const API_URL = "/admin/cl";

export const useClassLoaderAPI = () => {
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
                        `Successfully created class loader: ${newClassLoader.name}`
                    );
                })
                .catch(displayError);
        },
        [fetchClassLoaders, displayError, displaySuccess]
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
                        `Successfully deleted class loader${
                            classLoaderIds.length > 1 ? "s" : ""
                        }`
                    );
                })
                .catch(displayError);
        },
        [fetchClassLoaders, displayError, displaySuccess]
    );

    const updateClassLoader = useCallback(
        async (classLoader: ClassLoader) => {
            return APIService.put(`${API_URL}/${classLoader.id}`, classLoader)
                .then(() => {
                    fetchClassLoaders();
                    displaySuccess("Successfully saved class loader");
                })
                .catch(displayError);
        },
        [fetchClassLoaders, displayError, displaySuccess]
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

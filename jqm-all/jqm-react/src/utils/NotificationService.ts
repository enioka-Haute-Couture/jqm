import { useCallback } from "react";
import { useSnackbar } from "notistack";


export const useNotificationService = () => {
    const { enqueueSnackbar } = useSnackbar();

    const displayError = useCallback((reason: any) => {
        console.log(reason);
        enqueueSnackbar(
            "An error occured, please contact support support@enioka.com for help.",
            {
                variant: "error",
                persist: true,
            }
        );
    }, [enqueueSnackbar]);

    const displaySuccess = useCallback((message: string) => {
        enqueueSnackbar(
            message,
            {
                variant: "success",
            }
        );
    }, [enqueueSnackbar]);

    return { displayError, displaySuccess };
}

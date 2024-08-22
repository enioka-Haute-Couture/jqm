import { useCallback } from "react";
import { useSnackbar } from "notistack";
import React from "react";
import { Button } from "@mui/material";


export const useNotificationService = () => {
    const { enqueueSnackbar, closeSnackbar } = useSnackbar();


    const displayError = useCallback(
        (reason: any) => {

            const action = (key: any) => {
                return (<Button onClick={() => { closeSnackbar(key) }} sx={{ color: "white" }}>
                    Dismiss
                </Button>);
            }
            enqueueSnackbar(
                (reason?.details?.userReadableMessage) ?
                    reason.details.userReadableMessage :
                    "An error occured, please contact support support@enioka.com for help"
                ,
                {
                    variant: "error",
                    persist: true,
                    action
                }
            );
        },
        [enqueueSnackbar, closeSnackbar]
    );

    const displaySuccess = useCallback(
        (message: string) => {
            enqueueSnackbar(message, {
                variant: "success",
            });
        },
        [enqueueSnackbar]
    );

    return { displayError, displaySuccess };
};

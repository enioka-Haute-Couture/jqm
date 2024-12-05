import { useCallback } from "react";
import { useSnackbar } from "notistack";
import React from "react";
import { Button } from "@mui/material";


export const useNotificationService = () => {
    const { enqueueSnackbar, closeSnackbar } = useSnackbar();


    const displayError = useCallback(
        (reason: any) => {

            let message = reason?.details?.userReadableMessage ?
                reason.details.userReadableMessage :
                "An error occured, please contact support support@enioka.com for help"

            // make sure the letter starts with a capital letter
            message = message.charAt(0).toUpperCase() + message.slice(1);

            const action = (key: any) => {
                return (<Button onClick={() => { closeSnackbar(key) }} sx={{ color: "white" }}>
                    Dismiss
                </Button>);
            }
            enqueueSnackbar(
                message,
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

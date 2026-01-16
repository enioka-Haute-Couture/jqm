import { useCallback } from "react";
import { useSnackbar } from "notistack";
import React from "react";
import { Button } from "@mui/material";
import { useTranslation } from "react-i18next";


export const useNotificationService = () => {
    const { t } = useTranslation();
    const { enqueueSnackbar, closeSnackbar } = useSnackbar();


    const displayError = useCallback(
        (reason: any) => {

            let message = reason?.details?.userReadableMessage ?
                reason.details.userReadableMessage :
                t("errors.genericError")

            // make sure the letter starts with a capital letter
            message = message.charAt(0).toUpperCase() + message.slice(1);

            const action = (key: any) => {
                return (<Button onClick={() => { closeSnackbar(key) }} sx={{ color: "white" }}>
                    {t("errors.dismiss")}
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
        [enqueueSnackbar, closeSnackbar, t]
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

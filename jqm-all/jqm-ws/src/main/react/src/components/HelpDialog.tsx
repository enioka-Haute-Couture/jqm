import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from "@mui/material";
import React, { ReactNode } from "react";
import { useTranslation } from "react-i18next";

export const HelpDialog: React.FC<{
    isOpen: boolean;
    onClose: () => void;
    title: string;
    header: string;
    descriptionParagraphs: ReactNode[];
}> = ({ isOpen, onClose: closeDialog, title, header, descriptionParagraphs }) => {
    const { t } = useTranslation();
    return (
        <Dialog
            open={isOpen}
            onClose={closeDialog}
            fullWidth
            maxWidth={"lg"}
        >
            <DialogTitle>{title}</DialogTitle>
            <DialogContent>
                <Typography sx={{ fontWeight: 'bold' }}>
                    {header}
                </Typography>
                {descriptionParagraphs.map((p, i) => (<Typography sx={{ mt: 2 }} key={i}>{p}</Typography>))}
            </DialogContent>
            <DialogActions>
                <Button
                    variant="contained"
                    size="small"
                    onClick={closeDialog}
                    style={{ margin: "8px" }}
                >
                    {t("common.close")}
                </Button>
            </DialogActions>
        </Dialog>
    )
}

import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
} from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { JobDefinitionSchedule } from "./JobDefinition";
import { Queue } from "../Queues/Queue";

export const ViewSchedulesDialog: React.FC<{
    closeDialog: () => void;
    schedules: Array<JobDefinitionSchedule>;
    queues: Queue[];
}> = ({ closeDialog, schedules, queues }) => {
    const { t } = useTranslation();

    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"md"}
        >
            <DialogTitle id="form-dialog-title">
                {t("jobDefinitions.viewSchedulesDialog.title")}
            </DialogTitle>
            <DialogContent>
                <TableContainer component={Paper}>
                    <Table size="small" aria-label="Schedules">
                        <TableHead>
                            <TableRow>
                                <TableCell>
                                    {t("jobDefinitions.viewSchedulesDialog.cronExpressionColumn")}
                                </TableCell>
                                <TableCell>
                                    {t("jobDefinitions.viewSchedulesDialog.queueOverrideColumn")}
                                </TableCell>
                                <TableCell>
                                    {t("jobDefinitions.viewSchedulesDialog.parametersColumn")}
                                </TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {schedules.map((schedule, index) => (
                                <TableRow key={index}>
                                    <TableCell component="th" scope="row">
                                        {schedule.cronExpression}
                                    </TableCell>
                                    <TableCell>
                                        {
                                            queues.find(
                                                (queue) =>
                                                    queue.id === schedule.queue
                                            )?.name
                                        }
                                    </TableCell>
                                    <TableCell>
                                        {schedule.parameters
                                            .map(
                                                (parameter) =>
                                                    `${parameter.key}: ${parameter.value}`
                                            )
                                            .join(", ")}
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            </DialogContent>
            <DialogActions>
                <Button
                    variant="contained"
                    size="small"
                    style={{ margin: "8px" }}
                    onClick={closeDialog}
                    color="primary"
                >
                    {t("common.close")}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

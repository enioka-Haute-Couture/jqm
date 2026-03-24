import { TableContainer, TableBody, TableHead, Paper, Table, TableRow, TableCell, TextField, IconButton } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { JndiParameter } from "./JndiResource";
import DeleteIcon from "@mui/icons-material/Delete";


export const JndiParametersTable: React.FC<{
    parameters: JndiParameter[];
    setParameters?: (parameters: JndiParameter[]) => void;
}> = ({ parameters, setParameters }) => {
    const { t } = useTranslation();
    const readOnly = !setParameters;
    return (
        <TableContainer component={Paper}>
            <Table size="small">
                <TableHead>
                    <TableRow>
                        <TableCell>{t("jndi.editParametersDialog.name")}</TableCell>
                        <TableCell>{t("jndi.editParametersDialog.value")}</TableCell>
                        <TableCell align="right"></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {parameters.map(
                        (
                            {
                                key: label,
                                value,
                            }: {
                                key: string;
                                value: string | number | boolean;
                            },
                            index
                        ) => (
                            <TableRow key={`${label}-${index}`}>
                                <TableCell component="th" scope="row">
                                    {readOnly ? (
                                        label
                                    ) : (
                                        <TextField
                                            defaultValue={label}
                                            onChange={(evnt) => {
                                                parameters[index].key =
                                                    evnt.target.value;
                                            }}
                                            fullWidth
                                            margin="normal"
                                            inputProps={{
                                                style: { fontSize: "0.875rem" },
                                            }}
                                            variant="standard"
                                        />
                                    )}
                                </TableCell>
                                <TableCell>
                                    {readOnly ? (
                                        value
                                    ) : (
                                        <TextField
                                            defaultValue={value}
                                            onChange={(evnt) => {
                                                parameters[index].value =
                                                    evnt.target.value;
                                            }}
                                            fullWidth
                                            margin="normal"
                                            inputProps={{
                                                style: { fontSize: "0.875rem" },
                                            }}
                                            variant="standard"
                                        />
                                    )}
                                </TableCell>
                                <TableCell>
                                    {!readOnly && (
                                        <IconButton
                                            color="default"
                                            aria-label={"delete"}
                                            onClick={() =>
                                                setParameters(
                                                    parameters.filter(
                                                        (_, i) => i !== index
                                                    )
                                                )
                                            }
                                            size="large">
                                            <DeleteIcon />
                                        </IconButton>)
                                    }
                                </TableCell>
                            </TableRow>
                        )
                    )}
                </TableBody>
            </Table>
        </TableContainer>
    );
};

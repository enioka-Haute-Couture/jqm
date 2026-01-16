import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    FormControlLabel,
    FormHelperText,
    FormLabel,
    Input,
    InputLabel,
    MenuItem,
    Radio,
    RadioGroup,
    Select,
    SelectChangeEvent,
    TextField,
    Theme,
} from "@mui/material";
import { makeStyles } from "@mui/styles";
import React, { ReactNode, useState } from "react";
import { useTranslation } from "react-i18next";
import { JobDefinitionSpecificProperties, JobType } from "./JobDefinition";
import { ClassLoader } from "../ClassLoaders/ClassLoader";

const useStyles = makeStyles((theme: Theme) =>
({
    TextField: {
        padding: theme.spacing(0, 0, 3),
    },
})
);

export const SpecificPropertiesForm: React.FC<{
    jobType: JobType;
    jarPath: string;
    setJarPath: (jarPath: string) => void;
    javaClassName: string;
    setJavaClassName: (javaClassName: string) => void;
    pathType: string;
    setPathType: (pathType: string) => void;
    classLoaderId?: number;
    setClassLoaderId: (classLoaderId: number) => void;
    classLoaders: ClassLoader[];
}> = ({
    jobType,
    jarPath,
    setJarPath,
    javaClassName,
    setJavaClassName,
    pathType,
    setPathType,
    classLoaderId,
    setClassLoaderId,
    classLoaders
}) => {
        const { t } = useTranslation();
        const classes = useStyles();

        return (
            <>
                {jobType === JobType.java && (
                    <>
                        <TextField
                            className={classes.TextField}
                            label={t("jobDefinitions.editPropertiesDialog.pathToJarLabel")}
                            value={jarPath}
                            helperText={t("jobDefinitions.editPropertiesDialog.pathToJarHelper")}
                            onChange={(
                                event: React.ChangeEvent<HTMLInputElement>
                            ) => {
                                setJarPath(event.target.value);
                            }}
                            fullWidth
                            variant="standard"
                        />
                        <TextField
                            className={classes.TextField}
                            label={t("jobDefinitions.editPropertiesDialog.classToLaunchLabel")}
                            value={javaClassName}
                            helperText={t("jobDefinitions.editPropertiesDialog.classToLaunchHelper")}
                            onChange={(
                                event: React.ChangeEvent<HTMLInputElement>
                            ) => {
                                setJavaClassName(event.target.value);
                            }}
                            fullWidth
                            variant="standard"
                        />
                        <FormControl
                            fullWidth
                        >
                            <InputLabel id="class-loader-select-label">{t("jobDefinitions.editPropertiesDialog.classLoaderLabel")}</InputLabel>
                            <Select
                                fullWidth
                                value={classLoaderId || ''}
                                onChange={(event: SelectChangeEvent<number[] | number>, child: ReactNode) => {
                                    setClassLoaderId(event.target.value as number);
                                }}
                                input={<Input />}
                                labelId="class-loader-select-label"
                                label={t("jobDefinitions.editPropertiesDialog.classLoaderLabel")}
                            >
                                <MenuItem value=''>
                                    {t("jobDefinitions.editPropertiesDialog.classLoaderDefault")}
                                </MenuItem>
                                {classLoaders.map((cl: ClassLoader) => (
                                    <MenuItem key={cl.id} value={cl.id}>
                                        {cl.name}
                                    </MenuItem>
                                ))}
                            </Select>
                            <FormHelperText>{t("jobDefinitions.editPropertiesDialog.classLoaderHelper")}</FormHelperText>
                        </FormControl>
                    </>
                )
                }
                {
                    jobType === JobType.process && (
                        <TextField
                            className={classes.TextField}
                            label={t("jobDefinitions.editPropertiesDialog.pathToExecutableLabel")}
                            value={jarPath}
                            helperText={t("jobDefinitions.editPropertiesDialog.pathToExecutableHelper")}
                            onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                                setJarPath(event.target.value);
                            }}
                            fullWidth
                            variant="standard"
                        />
                    )
                }
                {
                    jobType === JobType.shell && (
                        <>
                            <FormControl
                                component="fieldset"
                                style={{ marginBottom: "16px" }}
                            >
                                <FormLabel component="legend">{t("jobDefinitions.editPropertiesDialog.shellLabel")}</FormLabel>
                                <RadioGroup
                                    aria-label="Shell"
                                    name="shell"
                                    value={pathType}
                                    onChange={(
                                        event: React.ChangeEvent<HTMLInputElement>
                                    ) => {
                                        setPathType(event.target.value);
                                    }}
                                >
                                    <FormControlLabel
                                        value="DEFAULTSHELLCOMMAND"
                                        control={<Radio />}
                                        label={t("jobDefinitions.editPropertiesDialog.defaultOsShellLabel")}
                                    />
                                    <FormControlLabel
                                        value="POWERSHELLCOMMAND"
                                        control={<Radio />}
                                        label={t("jobDefinitions.editPropertiesDialog.powershellLabel")}
                                    />
                                </RadioGroup>
                                <FormHelperText>
                                    {t("jobDefinitions.editPropertiesDialog.shellHelper")}
                                </FormHelperText>
                            </FormControl>
                            <TextField
                                className={classes.TextField}
                                label={t("jobDefinitions.editPropertiesDialog.shellCommandLabel")}
                                value={jarPath}
                                helperText={t("jobDefinitions.editPropertiesDialog.shellCommandHelper")}
                                onChange={(
                                    event: React.ChangeEvent<HTMLInputElement>
                                ) => {
                                    setJarPath(event.target.value);
                                }}
                                fullWidth
                                variant="standard"
                            />
                        </>
                    )
                }
            </>
        );
    };

export const EditSpecificPropertiesDialog: React.FC<{
    closeDialog: () => void;
    properties: JobDefinitionSpecificProperties;
    setProperties: (properties: JobDefinitionSpecificProperties) => void;
    classLoaders: ClassLoader[];
}> = ({ closeDialog, properties, setProperties, classLoaders }) => {
    const { t } = useTranslation();
    const [javaClassName, setJavaClassName] = useState<string>(
        properties.javaClassName
    );
    const [jarPath, setJarPath] = useState<string>(properties.jarPath);
    const [pathType, setPathType] = useState<string>(properties.pathType);
    const [classLoaderId, setClassLoaderId] = useState<number | undefined>(
        properties.classLoaderId
    )

    return (
        <Dialog
            open={true}
            onClose={closeDialog}
            aria-labelledby="form-dialog-title"
            fullWidth
            maxWidth={"md"}
        >
            <DialogTitle id="form-dialog-title">
                {t("jobDefinitions.editPropertiesDialog.title", { jobType: properties.jobType })}
            </DialogTitle>
            <DialogContent>
                <SpecificPropertiesForm
                    jobType={properties.jobType!!}
                    jarPath={jarPath}
                    setJarPath={setJarPath}
                    javaClassName={javaClassName}
                    setJavaClassName={setJavaClassName}
                    pathType={pathType}
                    setPathType={setPathType}
                    classLoaderId={classLoaderId}
                    setClassLoaderId={setClassLoaderId}
                    classLoaders={classLoaders}
                />
            </DialogContent>
            <DialogActions>
                <Button
                    size="small"
                    style={{ margin: "8px" }}
                    onClick={closeDialog}
                >
                    {t("common.cancel")}
                </Button>
                <Button
                    variant="contained"
                    size="small"
                    style={{ margin: "8px" }}
                    disabled={
                        !jarPath ||
                        (properties.jobType! === JobType.java && !javaClassName)
                    }
                    onClick={() => {
                        setProperties({
                            ...properties,
                            jarPath: jarPath!!,
                            javaClassName: javaClassName!!,
                            pathType: pathType,
                            classLoaderId: classLoaderId
                        });
                        closeDialog();
                    }}
                    color="primary"
                >
                    {t("jndi.editParametersDialog.validate")}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

import React, { ReactNode } from "react";
import MenuItem from "@mui/material/MenuItem";
import ListSubheader from "@mui/material/ListSubheader";
import FormControl from "@mui/material/FormControl";
import Select, { SelectChangeEvent } from "@mui/material/Select";
import { useTranslation } from "react-i18next";
import { resourceTemplates } from "./resourceTemplates";
import { JndiResource } from "./JndiResource";

const getResourceTemplate = (key: string) => ({
    ...resourceTemplates[key],
    resourceKey: key,
});

const resourceTemplatesList = [
    {
        titleKey: "jndi.resourceGroups.databasePools",
        resources: [
            getResourceTemplate("jndiOracle"),
            getResourceTemplate("jndiMySql"),
            getResourceTemplate("jndiPs"),
            getResourceTemplate("jndiHsqlDb"),
            getResourceTemplate("jndiOtherDb"),
        ],
    },
    {
        titleKey: "jndi.resourceGroups.messagesQ",
        resources: [
            getResourceTemplate("jndiMqQcf"),
            getResourceTemplate("jndiMqQ"),
            getResourceTemplate("jndiAmqQcf"),
            getResourceTemplate("jndiAmqQ"),
        ],
    },
    {
        titleKey: "jndi.resourceGroups.locators",
        resources: [
            getResourceTemplate("jndiFile"),
            getResourceTemplate("jndiUrl"),
            getResourceTemplate("jndiString"),
        ],
    },
    {
        titleKey: "jndi.resourceGroups.smtpMail",
        resources: [getResourceTemplate("jndiMail")],
    },
    {
        titleKey: "jndi.resourceGroups.genericResource",
        resources: [getResourceTemplate("jndiGeneric")],
    },
];

export const ResourceDropDownMenu: React.FC<{
    menuPositiontRef: React.MutableRefObject<null>;
    show: boolean;
    handleSet: (val: boolean) => void;
    onOpen: () => void;
    onClose: () => void;
    onSelectResource: (resource: JndiResource) => void;
}> = ({ menuPositiontRef, show, onClose, onOpen, onSelectResource }) => {
    const { t } = useTranslation();
    const selectGroupList: any[] = [];
    resourceTemplatesList.forEach(({ titleKey, resources }) => {
        selectGroupList.push(
            <ListSubheader key={titleKey} disableSticky>
                {t(titleKey)}
            </ListSubheader>
        );
        resources.map(({ uiName, resourceKey }) =>
            selectGroupList.push(
                <MenuItem key={resourceKey} value={resourceKey}>
                    {t(uiName || "")}
                </MenuItem>
            )
        );
    });

    return (
        <FormControl>
            <Select
                id="select"
                style={{ display: "none" }}
                open={show}
                value=""
                defaultValue=""
                onOpen={() => onOpen()}
                onClose={() => onClose()}
                onChange={
                    (event: SelectChangeEvent<string>, child: ReactNode) => {
                        const val = event.target.value as string;
                        const resource = resourceTemplates[val];
                        if (resource && resource.name) {
                            onSelectResource(resourceTemplates[val]);
                        }
                    }}
                MenuProps={{
                    anchorEl: menuPositiontRef.current,
                    style: { marginTop: "3.5rem" },
                }}
            >
                {selectGroupList}
            </Select>
        </FormControl>
    );
};

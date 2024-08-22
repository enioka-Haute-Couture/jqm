import React, { ReactNode } from "react";
import MenuItem from "@mui/material/MenuItem";
import ListSubheader from "@mui/material/ListSubheader";
import FormControl from "@mui/material/FormControl";
import Select, { SelectChangeEvent } from "@mui/material/Select";
import { resourceTemplates } from "./resourceTemplates";
import { JndiResource } from "./JndiResource";

const getResourceTemplate = (key: string) => ({
    ...resourceTemplates[key],
    resourceKey: key,
});

const resourceTemplatesList = [
    {
        title: "Database Pools",
        resources: [
            getResourceTemplate("jndiOracle"),
            getResourceTemplate("jndiMySql"),
            getResourceTemplate("jndiPs"),
            getResourceTemplate("jndiHsqlDb"),
            getResourceTemplate("jndiOtherDb"),
        ],
    },
    {
        title: "Messages Q & QCF",
        resources: [
            getResourceTemplate("jndiMqQcf"),
            getResourceTemplate("jndiMqQ"),
            getResourceTemplate("jndiAmqQcf"),
            getResourceTemplate("jndiAmqQ"),
        ],
    },
    {
        title: "Locators",
        resources: [
            getResourceTemplate("jndiFile"),
            getResourceTemplate("jndiUrl"),
            getResourceTemplate("jndiString"),
        ],
    },
    {
        title: "SMTP Mail",
        resources: [getResourceTemplate("jndiMail")],
    },
    {
        title: "Generic Resource",
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
    const selectGroupList: any[] = [];
    resourceTemplatesList.forEach(({ title, resources }) => {
        selectGroupList.push(
            <ListSubheader key={title} disableSticky>
                {title}
            </ListSubheader>
        );
        resources.map(({ uiName, resourceKey }) =>
            selectGroupList.push(
                <MenuItem key={resourceKey} value={resourceKey}>
                    {uiName}
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

export interface ModalTab {
    /** Text shown on the tab button */
    label: string;
    /** Plain content for now (can be upgraded to TemplateRef later) */
    content: string;
}

export interface ModalConfig {
    /** Title shown in the modal header */
    title?: string;
    /** Tabs to render. 0, 1, or many allowed. */
    tabs: ModalTab[];
    initialState?: Record<string, any>
    /** Optional width constraints */
    width?: string;
    maxWidth?: string;
}

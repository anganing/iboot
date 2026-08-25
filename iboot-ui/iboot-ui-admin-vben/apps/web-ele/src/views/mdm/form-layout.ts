export type MdmFormColumns = 1 | 2 | 3;

const FORM_LAYOUTS = {
  1: {
    formItemClass: 'col-span-1',
    modalClass: 'w-130',
    wrapperClass: 'grid-cols-1',
  },
  2: {
    formItemClass: 'col-span-1',
    modalClass: 'w-[900px]',
    wrapperClass: 'grid-cols-1 md:grid-cols-2',
  },
  3: {
    formItemClass: 'col-span-1',
    modalClass: 'w-[1200px]',
    wrapperClass: 'grid-cols-1 md:grid-cols-2 xl:grid-cols-3',
  },
} as const;

export function getMdmFormLayout(
  columns: MdmFormColumns = 1,
  modalClass?: string,
) {
  return {
    ...FORM_LAYOUTS[columns],
    modalClass: modalClass ?? FORM_LAYOUTS[columns].modalClass,
  };
}

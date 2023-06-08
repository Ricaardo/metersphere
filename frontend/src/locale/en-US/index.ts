import dayjsLocale from 'dayjs/locale/en';
import localeSettings from './settings';
import sys from './sys';
import localeMessageBox from '@/components/message-box/locale/en-US';
import minder from '@/components/minder-editor/locale/en-US';
import localeLogin from '@/views/login/locale/en-US';
import localeTable from '@/components/ms-table/locale/en-US';
import localeApiTest from '@/views/api-test/locale/en-US';

export default {
  message: {
    'menu.apiTest': 'Api Test',
    'navbar.action.locale': 'Switch to English',
    ...sys,
    ...localeSettings,
    ...localeMessageBox,
    ...localeLogin,
    ...minder,
    ...localeTable,
    ...localeApiTest,
  },
  dayjsLocale,
  dayjsLocaleName: 'en-US',
};

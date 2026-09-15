import {attachKensaEmbeds} from './embedHost';

// Host page script. Include it once and every <iframe data-kensa-embed> on the
// page takes the height its report posts. Frames are looked up per message, so
// one added after load is picked up without any re-scan.

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => attachKensaEmbeds());
} else {
    attachKensaEmbeds();
}

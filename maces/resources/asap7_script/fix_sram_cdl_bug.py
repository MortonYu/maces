import sys
import re
import os
import tempfile
import shutil

"""
vendor's SRAM cdl use slvt cell, this patch will sed cells name in which, fix this bug.
"""

extracted_tarballs_dir = sys.argv[1]
pattern0 = re.compile("SL")
pattern1 = re.compile("slvt")

with tempfile.NamedTemporaryFile(delete=False) as tf:
    sram_cdl = os.path.join(extracted_tarballs_dir, "ASAP7_PDKandLIB.tar/ASAP7_PDKandLIB_v1p5/asap7libs_24.tar.bz2/asap7libs_24/cdl/lvs/asap7_75t_SRAM.cdl")
    with open(sram_cdl, 'r') as f:
        tf.write(pattern1.sub("sram", pattern0.sub("SRAM", f.read())).encode('utf-8'))
    shutil.copystat(sram_cdl, tf.name)
    shutil.copy(tf.name, sram_cdl)

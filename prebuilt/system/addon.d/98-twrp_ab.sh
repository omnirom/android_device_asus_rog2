#!/system/bin/sh
#
# ADDOND_VERSION=2
#
# /system/addon.d/98-twrp_ab.sh
#
# Keep TWRP installed in a boot partition through A/B OTAs
# with Magisk installed.
#
# osm0sis @ xda-developers
#

. /postinstall/tmp/backuptool.functions

initialize() {
  BOOTMODE=false;
  ps | grep zygote | grep -v grep >/dev/null && BOOTMODE=true;
  $BOOTMODE || ps -A 2>/dev/null | grep zygote | grep -v grep >/dev/null && BOOTMODE=true

  MAGISKBIN=/data/adb/magisk

  if [ ! -d $MAGISKBIN ]; then
    # update-binary|updater <RECOVERY_API_VERSION> <OUTFD> <ZIPFILE>
    OUTFD=$(ps | grep -v 'grep' | grep -oE 'update(.*) 3 [0-9]+' | cut -d" " -f3)
    [ -z $OUTFD ] && OUTFD=$(ps -Af | grep -v 'grep' | grep -oE 'update(.*) 3 [0-9]+' | cut -d" " -f3)
    # update_engine_sideload --payload=file://<ZIPFILE> --offset=<OFFSET> --headers=<HEADERS> --status_fd=<OUTFD>
    [ -z $OUTFD ] && OUTFD=$(ps | grep -v 'grep' | grep -oE 'status_fd=[0-9]+' | cut -d= -f2)
    [ -z $OUTFD ] && OUTFD=$(ps -Af | grep -v 'grep' | grep -oE 'status_fd=[0-9]+' | cut -d= -f2)
    ui_print() { $BOOTMODE && log -t TWRP -- "$1" || echo -e "ui_print $1\nui_print" >> $OUTFD; }

    ui_print "**************************"
    ui_print "* TWRP addon.d-v2 failed"
    ui_print "**************************"
    ui_print "! Cannot find Magisk binaries"
    exit 1
  fi

  # Load utility functions
  . $MAGISKBIN/util_functions.sh

  if $BOOTMODE; then
    # Override ui_print when booted
    ui_print() { log -t TWRP -- "$1"; }
  else
    OUTFD=
    setup_flashable
  fi
}

unpack_slot() {
  find_boot_image

  ui_print "- Unpacking boot$SLOT image"
  $MAGISKBIN/magiskboot unpack "$BOOTIMAGE" || abort "! Unable to unpack boot image"

  eval $BOOTSIGNER -verify < $BOOTIMAGE && BOOTSIGNED=true
  $BOOTSIGNED && ui_print "- Boot image is signed with AVB 1.0"
}

main() {
  # /dev/tmp is safe for both booted and recovery installs
  rm -rf $TMPDIR
  mkdir -p $TMPDIR
  cd $TMPDIR

  $BOOTMODE || recovery_actions

  ui_print "*******************************"
  ui_print "* TWRP A/B addon.d-v2"
  ui_print "* by osm0sis @ xda-developers"
  ui_print "*******************************"

  mount_partitions
  check_data
  get_flags

  # Current SLOT should already be set by mount_partitions() in module backend
  [ -z $SLOT ] && abort "! For use on A/B slot devices only"

  # Resolve APK for BOOTSIGNER functionality
  find_manager_apk

  unpack_slot

  $MAGISKBIN/magiskboot cpio ramdisk.cpio "exists twres" || abort "! TWRP ramdisk not found"

  ui_print "- Backing up TWRP ramdisk"
  cp -f ramdisk.cpio ramdisk.cpio.orig

  $MAGISKBIN/magiskboot cleanup

  # Switch to alternate SLOT for remaining partition actions
  case $SLOT in
    _a) SLOT=_b;;
    _b) SLOT=_a;;
  esac

  unpack_slot

  ui_print "- Replacing ramdisk with TWRP backup"
  mv -f ramdisk.cpio.orig ramdisk.cpio

  ui_print "- Repacking boot image"
  $MAGISKBIN/magiskboot repack "$BOOTIMAGE" || abort "! Unable to repack boot image"

  $MAGISKBIN/magiskboot cleanup

  flash_image new-boot.img "$BOOTIMAGE"

  # Cleanups
  cd /
  $BOOTMODE || recovery_cleanup
  rm -rf $TMPDIR

  ui_print "- Done"
  exit 0
}

case "$1" in
  backup)
    # Stub
  ;;
  restore)
    # Stub
  ;;
  pre-backup)
    # Stub
  ;;
  post-backup)
    # Stub
  ;;
  pre-restore)
    # Stub
  ;;
  post-restore)
    initialize
    su=sh
    $BOOTMODE && su=su
    exec $su -c "sh $0 addond-v2"
  ;;
  addond-v2)
    initialize
    main
  ;;
esac

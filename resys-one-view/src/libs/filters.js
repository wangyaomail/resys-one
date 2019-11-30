export const formatTime = val => {
  if (val) {
    let theTime = parseInt(val);
    let theTime1 = 0;
    let theTime2 = 0;
    if (theTime > 60) {
      theTime1 = parseInt(theTime / 60);
      theTime = parseInt(theTime % 60);
      if (theTime1 > 60) {
        theTime2 = parseInt(theTime1 / 60);
        theTime1 = parseInt(theTime1 % 60);
      }
    }
    let result = "" + parseInt(theTime) + "秒";
    if (theTime1 > 0) {
      result = "" + parseInt(theTime1) + "分" + result;
    }
    if (theTime2 > 0) {
      result = "" + parseInt(theTime2) + "小时" + result;
    }
    return result;
  } else {
    return "-";
  }
};

export const formatDate = val => {
  if (val) {
    let date = new Date(val);
    let dateStr =  date.getFullYear() + "-" + (date.getMonth() < 9 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1) + "-" + (date.getDate() < 10 ? "0" + date.getDate() : date.getDate());
    return dateStr;
  } else {
    return "-";
  }
};

export const formatFullDate = val => {
  if (val) {
    let date = new Date(val);
    // let currentY = new Date().getFullYear();
    return date.getFullYear() +
      "-" + (date.getMonth() < 9 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1) +
      "-" + (date.getDate() < 10 ? "0" + date.getDate() : date.getDate()) +
      " " + (date.getHours() < 10 ? "0" + date.getHours() : date.getHours()) +
      ":" + (date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes()) +
      ":" + (date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds());
  } else {
    return "-";
  }
};

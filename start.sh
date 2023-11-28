function start {
  docker compose up -d
}

function stop {
  docker compose stop
}

function restart {
  docker compose restart
}

case $1 in

start)
  start
  ;;

stop)
  stop
  ;;

restart)
  restart
  ;;

*)
  echo "usage: $0 {start|stop|restart}"
  exit 1
  ;;

esac

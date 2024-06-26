package jmri.jmrit.timetable;

/**
 * Define the content of a Train record.
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class Train {

    /**
     * Create a new train with default values.
     * @param scheduleId The parent schedule id.
     * @throws IllegalArgumentException TRAIN_ADD_FAIL
     */
    public Train(int scheduleId) {
        if (_dm.getSchedule(scheduleId) == null) {
            throw new IllegalArgumentException(TimeTableDataManager.TRAIN_ADD_FAIL);
        }
        _trainId = _dm.getNextId("Train");  // NOI18N
        _scheduleId = scheduleId;
        _dm.addTrain(_trainId, this);
    }

    public Train(int trainId, int scheduleId, int typeId, String trainName, String trainDesc,
                int defaultSpeed, int startTime, int throttle, int routeDuration, String trainNotes) {
        _trainId = trainId;
        _scheduleId = scheduleId;
        setTypeId(typeId);
        setTrainName(trainName);
        setTrainDesc(trainDesc);
        setDefaultSpeed(defaultSpeed);
        setStartTime(startTime);
        setThrottle(throttle);
        setRouteDuration(routeDuration);
        setTrainNotes(trainNotes);
    }

    TimeTableDataManager _dm = TimeTableDataManager.getDataManager();

    private final int _trainId;
    private final int _scheduleId;
    private int _typeId = 0;
    private String _trainName = Bundle.getMessage("NewTrainName");  // NOI18N
    private String _trainDesc = Bundle.getMessage("NewTrainDesc");  // NOI18N
    private int _defaultSpeed = 1;
    private int _startTime = 0;
    private int _throttle = 0;
    private int _routeDuration = 0;
    private String _trainNotes = "";

    /**
     * Make a copy of the train.
     * @param schedId The new schedule id, if zero use the current schedule id.
     * @param typeId The new train type id.  If zero use the current train type id.
     * @return a new Train instance.
     */
    public Train getCopy(int schedId, int typeId) {
        if (schedId == 0) schedId = getScheduleId();
        if (typeId == 0) typeId = getTypeId();

        Train copy = new Train(schedId);
        copy.setTypeId(typeId);
        copy.setTrainName(Bundle.getMessage("DuplicateCopyName", _trainName));
        copy.setTrainDesc(Bundle.getMessage("DuplicateCopyName", _trainDesc));
        copy.setDefaultSpeed(_defaultSpeed);
        copy.setStartTime(_startTime);
        copy.setThrottle(_throttle);
        copy.setRouteDuration(_routeDuration);
        copy.setTrainNotes(_trainNotes);
        return copy;
    }

    public int getTrainId() {
        return _trainId;
    }

    public int getScheduleId() {
        return _scheduleId;
    }

    public int getTypeId() {
        return _typeId;
    }

    public void setTypeId(int newType) {
        _typeId = newType;
    }

    public String getTrainName() {
        return _trainName;
    }

    public void setTrainName(String newName) {
        _trainName = newName;
    }

    public String getTrainDesc() {
        return _trainDesc;
    }

    public void setTrainDesc(String newDesc) {
        _trainDesc = newDesc;
    }

    public int getDefaultSpeed() {
        return _defaultSpeed;
    }

    public void setDefaultSpeed(int newSpeed) {
        if (newSpeed < 0) {
            throw new IllegalArgumentException(TimeTableDataManager.DEFAULT_SPEED_LT_0);
        }
        int oldSpeed = _defaultSpeed;
        _defaultSpeed = newSpeed;

        try {
            _dm.calculateTrain(_trainId, false);
            _dm.calculateTrain(_trainId, true);
        } catch (IllegalArgumentException ex) {
            _defaultSpeed = oldSpeed;  // Roll back default speed change
            throw ex;
        }
    }

    public int getStartTime() {
        return _startTime;
    }

    public void setStartTime(int newStartTime) {
        Schedule schedule = _dm.getSchedule(_scheduleId);
        if (!_dm.validateTime(schedule.getStartHour(), schedule.getDuration(), newStartTime)) {
            throw new IllegalArgumentException(String.format("%s~%d~%d",  // NOI18N
                    TimeTableDataManager.START_TIME_RANGE, schedule.getStartHour(), schedule.getStartHour() + schedule.getDuration()));
        }
        int oldStartTime = _startTime;
        _startTime = newStartTime;

        try {
            _dm.calculateTrain(_trainId, false);
            _dm.calculateTrain(_trainId, true);
        } catch (IllegalArgumentException ex) {
            _startTime = oldStartTime;  // Roll back start time change
            throw ex;
        }
    }

    public int getThrottle() {
        return _throttle;
    }

    public void setThrottle(int newThrottle) {
        Layout layout = _dm.getLayout(_dm.getSchedule(_scheduleId).getLayoutId());
        if (newThrottle < 0 || newThrottle > layout.getThrottles()) {
            throw new IllegalArgumentException(TimeTableDataManager.THROTTLE_RANGE);
        }

        _throttle = newThrottle;
    }

    public int getRouteDuration() {
        return _routeDuration;
    }

    public void setRouteDuration(int newRouteDuration) {
        _routeDuration = newRouteDuration;
    }

    public String getTrainNotes() {
        return _trainNotes;
    }

    public void setTrainNotes(String newNotes) {
        _trainNotes = newNotes;
    }

    @Override
    public String toString() {
        return _trainName;
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Train.class);
}

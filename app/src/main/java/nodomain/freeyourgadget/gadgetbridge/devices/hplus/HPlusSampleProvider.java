package nodomain.freeyourgadget.gadgetbridge.devices.hplus;


/*
* @author João Paulo Barraca &lt;jpbarraca@gmail.com&gt;
*/

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.query.QueryBuilder;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.devices.SampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.entities.HPlusHealthActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.HPlusHealthActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;

public class HPlusSampleProvider extends AbstractSampleProvider<HPlusHealthActivitySample> {


    private GBDevice mDevice;
    private DaoSession mSession;

    public HPlusSampleProvider(GBDevice device, DaoSession session) {
        super(device, session);

        mSession = session;
        mDevice = device;;
    }

    public int getID() {
        return SampleProvider.PROVIDER_HPLUS;
    }

    public int normalizeType(int rawType) {
        return rawType;
    }

    public int toRawActivityKind(int activityKind) {
        return activityKind;
    }


    @Override
    public List<HPlusHealthActivitySample> getAllActivitySamples(int timestamp_from, int timestamp_to) {
        List<HPlusHealthActivitySample> samples = super.getGBActivitySamples(timestamp_from, timestamp_to, ActivityKind.TYPE_ALL);

        Device dbDevice = DBHelper.findDevice(getDevice(), getSession());
        if (dbDevice == null) {
            // no device, no samples
            return Collections.emptyList();
        }

        QueryBuilder<HPlusHealthActivitySample> qb = getSession().getHPlusHealthActivitySampleDao().queryBuilder();

        qb.where(HPlusHealthActivitySampleDao.Properties.DeviceId.eq(dbDevice.getId()), HPlusHealthActivitySampleDao.Properties.Timestamp.ge(timestamp_from))
                .where(HPlusHealthActivitySampleDao.Properties.Timestamp.le(timestamp_to));

        List<HPlusHealthActivitySample> sampleList = qb.build().list();

        for (HPlusHealthActivitySample sample : sampleList) {
            if (timestamp_from <= sample.getTimestamp() && sample.getTimestamp() < timestamp_to) {
                sample.setRawKind(sample.getRawKind());
            }
        }
        detachFromSession();
        return samples;
    }
    @NonNull
    @Override
    protected de.greenrobot.dao.Property getTimestampSampleProperty() {
        return HPlusHealthActivitySampleDao.Properties.Timestamp;
    }

    @Override
    public HPlusHealthActivitySample createActivitySample() {
        return new HPlusHealthActivitySample();
    }

    @Override
    protected de.greenrobot.dao.Property getRawKindSampleProperty() {
        return HPlusHealthActivitySampleDao.Properties.RawKind;
    }

    @Override
    public float normalizeIntensity(int rawIntensity) {
        return rawIntensity; //TODO: Calculate actual value
    }

    @NonNull
    @Override
    protected de.greenrobot.dao.Property getDeviceIdentifierSampleProperty() {
        return HPlusHealthActivitySampleDao.Properties.DeviceId;
    }

    @Override
    public AbstractDao<HPlusHealthActivitySample, ?> getSampleDao() {
        return getSession().getHPlusHealthActivitySampleDao();
    }
}

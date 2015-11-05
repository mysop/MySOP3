package Ormlite;

import java.sql.SQLException;
import java.util.List;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
//import com.ck.ap.DatabaseHelper;

//【sop_master Dao】
public class sop_masterDao
{
    /* insert */
    public static int insert(DatabaseHelper databaseHelper, sop_masterVo sop_masterVo) {
        RuntimeExceptionDao<sop_masterVo, Integer> sop_masterDao = databaseHelper.getSop_masterDao();
        if (exist(databaseHelper, sop_masterVo)) {
            return 0;
        }
        return sop_masterDao.create(sop_masterVo);
    }

	/* exist */
    public static boolean exist(DatabaseHelper databaseHelper, sop_masterVo sop_masterVo) {
        RuntimeExceptionDao<sop_masterVo, Integer>sop_masterDao = databaseHelper
                .getSop_masterDao();
        QueryBuilder<sop_masterVo, Integer> queryBuilder = sop_masterDao
                .queryBuilder();
        try {
            queryBuilder.where()
                    .eq(sop_masterVo.FIELD_Sop_number, sop_masterVo.getSop_number());
            //	.and()
            //	.eq(AccountVo.FIELD_Device, aAccountVo.getDevice());
            return queryBuilder.query().size() > 0 ? true : false;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /* update */
    public static int update(DatabaseHelper databaseHelper, sop_masterVo sop_masterVo) {
        RuntimeExceptionDao<sop_masterVo, Integer> sop_masterDao = databaseHelper
                .getSop_masterDao();
        try {
            return sop_masterDao.update(sop_masterVo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /* delete */
    public static int delete(DatabaseHelper databaseHelper, sop_masterVo sop_masterVo) {
        RuntimeExceptionDao<sop_masterVo, Integer> sop_masterDao = databaseHelper
                .getSop_masterDao();
        try {
            return sop_masterDao.delete(sop_masterVo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /* select by id */
    public static sop_masterVo select(DatabaseHelper databaseHelper, int id) {
        RuntimeExceptionDao<sop_masterVo, Integer> sop_masterDao = databaseHelper
                .getSop_masterDao();
        try {
            return sop_masterDao.queryForId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* selectRaw */
    public static sop_masterVo getSop_masterVo(DatabaseHelper databaseHelper) {
        RuntimeExceptionDao<sop_masterVo, Integer> sop_masterDao = databaseHelper
                .getSop_masterDao();
        QueryBuilder<sop_masterVo, Integer> queryBuilder = sop_masterDao
                .queryBuilder();
        try {

            List<sop_masterVo> data = queryBuilder.where().raw("1=1").query();
            if (data.size() > 0) {
                return data.get(0);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
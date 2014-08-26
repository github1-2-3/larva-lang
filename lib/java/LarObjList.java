//列表类型
public final class LarObjList extends LarSeqObj
{
    private static final int MAX_SIZE = Integer.MIN_VALUE >>> 1; //最大元素个数

    public LarObj[] m_list;

    LarObjList()
    {
        m_list = new LarObj[8];
        m_len = 0; //用m_len来记录实际长度
    }

    LarObjList(int hint_size) throws Exception
    {
        this();
        adjust_size(hint_size);
    }

    LarObjList(LarObj obj) throws Exception
    {
        this();
        extend(obj);
    }

    private void extend(LarObj obj) throws Exception
    {
        for (LarObj iter = obj.f_iterator(); iter.f_has_next().op_bool();)
        {
            f_add(iter.f_next());
        }
    }

    private void adjust_size(int hint_size) throws Exception
    {
        if (hint_size < 0 || hint_size > MAX_SIZE)
        {
            throw new Exception("list大小超限");
        }
        int size = m_list.length;
        if (size >= hint_size)
        {
            return;
        }
        while (size < hint_size)
        {
            size <<= 1;
        }
        LarObj[] new_list = new LarObj[size];
        for (int i = 0; i < m_len; ++ i)
        {
            new_list[i] = m_list[i];
        }
        m_list = new_list;
    }
    
    public String get_type_name()
    {
        return "list";
    }

    public LarObj seq_get_item(int index) throws Exception
    {
        return m_list[index];
    }
    public void seq_set_item(int index, LarObj obj) throws Exception
    {
        m_list[index] = obj;
    }
    public LarObj seq_get_slice(int start, int end, int step) throws Exception
    {
        LarObjList list = new LarObjList();
        if (step > 0)
        {
            while (start < end)
            {
                list.f_add(m_list[start]);
                start += step;
            }
        }
        else
        {
            while (start > end)
            {
                list.f_add(m_list[start]);
                start += step;
            }
        }
        return list;
    }

    public LarObj op_add(LarObj obj) throws Exception
    {
        if (obj instanceof LarObjList)
        {
            //列表连接
            LarObjList list = (LarObjList)obj;
            LarObjList new_list = new LarObjList(m_len + list.m_len);
            for (int i = 0; i < m_len; ++ i)
            {
                new_list.m_list[new_list.m_len] = m_list[i];
                ++ new_list.m_len;
            }
            for (int i = 0; i < list.m_len; ++ i)
            {
                new_list.m_list[new_list.m_len] = list.m_list[i];
                ++ new_list.m_len;
            }
            return new_list;
        }
        return obj.op_reverse_add(this);
    }
    public LarObj op_inplace_add(LarObj obj) throws Exception
    {
        //+=运算，相当于将obj中的元素都添加过来
        extend(obj);
        return this; //根据增量赋值运算规范，返回自身
    }
    public LarObj op_mul(LarObj obj) throws Exception
    {
        long times = obj.op_int();
        if (times < 0)
        {
            throw new Exception("list乘以负数");
        }
        if (times == 0)
        {
            return new LarObjList();
        }
        if (MAX_SIZE / times < m_len) //这个判断要考虑溢出，不能m_len * times > MAX_SIZE
        {
            throw new Exception("list大小超限");
        }
        LarObjList new_list = new LarObjList(m_len * (int)times);
        for (; times > 0; -- times)
        {
            for (int i = 0; i < m_len; ++ i)
            {
                new_list.m_list[new_list.m_len] = m_list[i];
                ++ new_list.m_len;
            }
        }
        return new_list;
    }
    public LarObj op_reverse_mul(LarObj obj) throws Exception
    {
        return op_mul(obj); //交换律
    }

    public boolean op_contain(LarObj obj) throws Exception
    {
        for (int i = 0; i < m_len; ++ i)
        {
            if (m_list[i].op_eq(obj))
            {
                return true;
            }
        }
        return false;
    }

    public LarObj f_add(LarObj obj) throws Exception
    {
        if (m_len == m_list.length)
        {
            adjust_size(m_len + 1);
        }
        m_list[m_len] = obj;
        ++ m_len;
        return this;
    }

    //懒得考虑qsort的太多因素，直接用shell了，序列是Sedgewick的
    static final int[] INC_LIST = new int[]{
        1073643521, 603906049, 268386305, 150958081, 67084289, 37730305, 16764929, 9427969, 4188161, 2354689, 1045505,
        587521, 260609, 146305, 64769, 36289, 16001, 8929, 3905, 2161, 929, 505, 209, 109, 41, 19, 5, 1};
    public LarObj f_sort() throws Exception
    {
        for (int inc_idx = 0; inc_idx < INC_LIST.length; ++ inc_idx)
        {
            int inc = INC_LIST[inc_idx];
            for (int i = inc; i < m_len; ++ i)
            {
                LarObj tmp = m_list[i];
                int j;
                for (j = i; j >= inc; j -= inc)
                {
                    if (m_list[j - inc].op_cmp(tmp) <= 0)
                    {
                        break;
                    }
                    m_list[j] = m_list[j - inc];
                }
                m_list[j] = tmp;
            }
        }
        return this;
    }
    
    public LarObj f_pop() throws Exception
    {
        if (m_len > 0)
        {
            -- m_len;
            LarObj obj = m_list[m_len];
            m_list[m_len] = null;
            return obj;
        }
        throw new Exception("从空list中pop");
    }
}
